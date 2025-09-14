'use client';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import Image from 'next/image';
import {
    Application,
    Container,
    Graphics,
    Sprite,
    Text,
    Texture,
    Rectangle,
    Assets,
} from 'pixi.js';
import TextButton from '@/components/atoms/buttons/text-button';
import resourceBack from '@/resources/pick-and-aim/resource-paa-back.svg';
import { useRouter, useSearchParams } from 'next/navigation';
import resourceTarget from '@/resources/pick-and-aim/resource-target.png';
import resourceReturn from '@/resources/pick-and-aim/resource-paa-return.svg';
// resourceCrosshair was used before when crosshair was a sprite; retained here for reference
import resourceBackground from '@/resources/pick-and-aim/resource-background.png';
import LoadingIcon from '@/components/atoms/contents/loading-icon';

type Keyword = { key: string; displayName: string; description: string };

const MAX_SELECTION = 3;
const MAX_AMMO = 3;
const ROWS = 3;
const TARGET_COUNT = 15;
// UI tuning knobs
const TARGET_SCALE = 0.16; // 과녁 크기 축소
const CROSSHAIR_SCALE = 2.0; // 크로스헤어 크기 (기본 0.9)
const HIT_RADIUS = 70; // 히트 판정 반경(px)
const BACKGROUND_SCALE = 1.0; // 배경 스케일 (1: 꽉 채움, <1: 축소)
const CROSSHAIR_DEFAULT_TINT = 0x000000;
const CROSSHAIR_AIM_TINT = 0xff4343; // FPS Red

type TargetNode = {
    container: Container;
    sprite: Sprite;
    label: Text;
    vx: number;
    row: number;
    alive: boolean;
    keyword: string;
};

export default function PickAndAimShotKeywordSelect() {
    const mountRef = useRef<HTMLDivElement | null>(null);
    const targetsRef = useRef<TargetNode[]>([]);
    const poolRef = useRef<TargetNode[]>([]);
    const crosshairRef = useRef<Graphics | null>(null);
    const ammoRef = useRef<number>(MAX_AMMO);
    const selectedRef = useRef<string[]>([]);
    const [selected, setSelected] = useState<string[]>([]);
    const [keywords, setKeywords] = useState<Keyword[]>([]);
    const [ammo, setAmmo] = useState<number>(MAX_AMMO);
    const [aimedDesc, setAimedDesc] = useState<string | null>(null);
    const lastDescRef = useRef<string | null>(null);
    const [loadingKeywords, setLoadingKeywords] = useState(true);
    const [loadingGame, setLoadingGame] = useState(true);
    // ADD: ref to hold latest keywords
    const keywordsRef = useRef<Keyword[]>([]);
    const nextKeywordIdxRef = useRef<number>(0);
    const getNextKeyword = (): Keyword => {
        const arr = keywordsRef.current;
        if (!arr || arr.length === 0)
            return { key: 'keyword', displayName: '키워드', description: '' };
        const idx = nextKeywordIdxRef.current % arr.length;
        nextKeywordIdxRef.current += 1;
        return arr[idx];
    };
    const selectedListText = useMemo(() => {
        if (!selected || selected.length === 0) return '키워드 없음';
        const names = selected.map(k => {
            const found = keywords.find(x => x.key === k);
            return found?.displayName || k;
        });
        return names.join(', ');
    }, [selected, keywords]);

    const router = useRouter();
    const params = useSearchParams();
    const game = params.get('game') || 'VALORANT';
    const leagueId = params.get('leagueId') || '0';

    // timers for per-row spawn scheduling
    const spawnTimersRef = useRef<number[]>([]);

    // REPLACE: spawnTarget definition to use useCallback and keywordsRef
    const spawnTarget = useCallback(
        (
            app: Application,
            targetTex: Texture,
            preferredRow?: number,
            forcedKeyword?: Keyword
        ) => {
            // ensure keywords are ready; avoid fallback label '키워드'
            if (!keywordsRef.current || keywordsRef.current.length === 0) {
                return;
            }
            let row =
                typeof preferredRow === 'number'
                    ? preferredRow
                    : Math.floor(Math.random() * ROWS);
            const yForRow = (r: number) =>
                ((r + 0.5) * app.screen.height) / ROWS;
            // slower overall speed: reduce max
            const speed = 0.8 + Math.random() * 0.9;
            const dir = Math.random() < 0.5 ? -1 : 1;
            // choose a keyword that is not already active on screen
            const active = new Set<string>(
                targetsRef.current.map(t => t.keyword)
            );
            const candidates = keywordsRef.current.filter(
                k => !active.has(k.key)
            );
            const pool =
                candidates.length > 0 ? candidates : keywordsRef.current;
            const pick =
                forcedKeyword ??
                pool[Math.floor(Math.random() * pool.length)] ??
                null;
            const keywordKey = pick?.key ?? 'keyword';
            const keywordLabel = pick?.displayName ?? '키워드';

            let node = poolRef.current.pop();
            if (!node) {
                const container = new Container();
                const sprite = new Sprite({ texture: targetTex });
                sprite.anchor.set(0.5);
                sprite.scale.set(TARGET_SCALE);
                const label = new Text({
                    text: keywordLabel,
                    style: { fill: 0xffffff, fontSize: 22, fontWeight: '800' },
                });
                label.anchor.set(0.5);
                container.addChild(sprite);
                container.addChild(label);
                node = {
                    container,
                    sprite,
                    label,
                    vx: 0,
                    row,
                    alive: true,
                    keyword: keywordKey,
                };
            } else {
                node.label.text = keywordLabel;
                node.keyword = keywordKey; // keep key for logic
                node.row = row;
                node.alive = true;
                node.container.visible = true;
                node.sprite.scale.set(TARGET_SCALE);
            }

            // choose a row with clearance near the entry edge to avoid crowded spawns
            const hasEntryClearance = (r: number): boolean => {
                const near = 280; // increased spacing near entry edge
                const aliveSameRow = targetsRef.current.filter(
                    t => t.alive && t.row === r
                );
                if (dir > 0) {
                    // entering from left, ensure no target too close to left edge
                    return !aliveSameRow.some(t => t.container.x < near);
                }
                // entering from right
                return !aliveSameRow.some(
                    t => t.container.x > app.screen.width - near
                );
            };
            const hasMinimumSpacing = (x: number, yPos: number): boolean => {
                const minDist = 180; // pixels
                for (const t of targetsRef.current) {
                    if (!t.alive) continue;
                    const dx = t.container.x - x;
                    const dy = yForRow(t.row) - yPos;
                    if (dx * dx + dy * dy < minDist * minDist) return false;
                }
                return true;
            };
            let attempts = 0;
            // try different rows to satisfy both entry clearance and global spacing
            while (attempts < ROWS * 3) {
                const xCandidate = dir > 0 ? -200 : app.screen.width + 200;
                if (
                    hasEntryClearance(row) &&
                    hasMinimumSpacing(xCandidate, yForRow(row))
                )
                    break;
                row =
                    typeof preferredRow === 'number'
                        ? preferredRow
                        : Math.floor(Math.random() * ROWS);
                attempts += 1;
            }
            // spawn from off-screen so it slides in
            node.container.x = dir > 0 ? -200 : app.screen.width + 200;
            node.container.y = yForRow(row);
            node.vx = speed * dir;

            app.stage.addChild(node.container);
            targetsRef.current.push(node);
        },
        []
    );

    useEffect(() => {
        if (loadingKeywords) return;
        const mountEl = mountRef.current;
        if (!mountEl) return;
        let destroyed = false;
        let app: Application | null = null;
        let resizeObserver: ResizeObserver | null = null;

        (async () => {
            try {
                app = new Application();
                await app.init({
                    width: mountEl.clientWidth || 800,
                    height: mountEl.clientHeight || 600,
                    background: 0xe0e0e0,
                    antialias: true,
                });
                if (destroyed || !app) return;
                mountEl.appendChild(app.canvas);
                app.canvas.style.cursor = 'none';

                if (destroyed || !app) return;

                // load textures explicitly to avoid race conditions
                const [targetTex, backgroundTex] = await Promise.all([
                    Assets.load(resourceTarget),
                    Assets.load(resourceBackground),
                ]);

                // sorting for layering
                app.stage.sortableChildren = true;

                // crosshair as Graphics for dynamic color
                const crosshair = new Graphics();
                const drawCrosshair = (color: number) => {
                    crosshair.clear();
                    crosshair
                        .circle(0, 0, 8 * CROSSHAIR_SCALE)
                        .stroke({ color, width: 2 * CROSSHAIR_SCALE })
                        .moveTo(-14 * CROSSHAIR_SCALE, 0)
                        .lineTo(14 * CROSSHAIR_SCALE, 0)
                        .moveTo(0, -14 * CROSSHAIR_SCALE)
                        .lineTo(0, 14 * CROSSHAIR_SCALE)
                        .stroke({ color, width: 2 * CROSSHAIR_SCALE });
                };
                drawCrosshair(CROSSHAIR_DEFAULT_TINT);
                crosshairRef.current = crosshair;
                // background tiling sprite
                const bg = new Sprite({ texture: backgroundTex });
                bg.anchor.set(0.5);
                bg.position.set(app.screen.width / 2, app.screen.height / 2);
                const scale =
                    Math.max(
                        app.screen.width / bg.texture.width,
                        app.screen.height / bg.texture.height
                    ) * BACKGROUND_SCALE;
                bg.scale.set(scale);
                bg.zIndex = 0;
                app.stage.addChildAt(bg, 0);

                crosshair.zIndex = 1000; // keep crosshair on top
                app.stage.addChild(crosshair);

                app.stage.eventMode = 'static';
                app.stage.hitArea = new Rectangle(
                    0,
                    0,
                    app.screen.width,
                    app.screen.height
                );

                const onResize = () => {
                    if (!app || destroyed) return;
                    const w = mountEl.clientWidth || 800;
                    const h = mountEl.clientHeight || 600;
                    app.renderer.resize(w, h);
                    app.stage.hitArea = new Rectangle(
                        0,
                        0,
                        app.screen.width,
                        app.screen.height
                    );
                    // resize background to cover
                    if (bg) {
                        bg.position.set(
                            app.screen.width / 2,
                            app.screen.height / 2
                        );
                        const s =
                            Math.max(
                                app.screen.width / bg.texture.width,
                                app.screen.height / bg.texture.height
                            ) * BACKGROUND_SCALE;
                        bg.scale.set(s);
                    }
                };
                resizeObserver = new ResizeObserver(onResize);
                resizeObserver.observe(mountEl);

                app.stage.on('pointermove', e => {
                    const p = e.global;
                    crosshair.position.set(p.x, p.y);
                });

                app.stage.on('pointertap', e => {
                    if (selectedRef.current.length >= MAX_SELECTION) return;
                    if (ammoRef.current <= 0) return;
                    const p = e.global;
                    let hit = false;
                    for (const t of targetsRef.current) {
                        if (!t.alive) continue;
                        const dx = p.x - t.container.x;
                        const dy = p.y - t.container.y;
                        if (dx * dx + dy * dy <= HIT_RADIUS * HIT_RADIUS) {
                            t.alive = false;
                            t.container.visible = false;
                            if (selectedRef.current.includes(t.keyword)) {
                                hit = true;
                                const g = new Graphics()
                                    .circle(p.x, p.y, 24)
                                    .fill(0xff4343);
                                app!.stage.addChild(g);
                                setTimeout(() => g.destroy(), 200);
                                break;
                            }
                            // 새 키워드라면 탄 감소 및 키워드 추가
                            selectedRef.current = [
                                ...selectedRef.current,
                                t.keyword,
                            ];
                            setSelected(selectedRef.current);
                            hit = true;
                            const g = new Graphics()
                                .circle(p.x, p.y, 24)
                                .fill(0xff4343);
                            app!.stage.addChild(g);
                            setTimeout(() => g.destroy(), 200);
                            // 탄 감소는 여기서만 실행
                            setAmmo(prev => {
                                const next = Math.max(0, prev - 1);
                                ammoRef.current = next;
                                return next;
                            });
                            break;
                        }
                    }
                    if (hit) {
                        // 명중 시 즉시 크로스헤어 색상 복귀
                        if (crosshairRef.current) {
                            const ch = crosshairRef.current;
                            ch.clear();
                            ch.circle(0, 0, 8 * CROSSHAIR_SCALE)
                                .stroke({
                                    color: CROSSHAIR_DEFAULT_TINT,
                                    width: 2 * CROSSHAIR_SCALE,
                                })
                                .moveTo(-14 * CROSSHAIR_SCALE, 0)
                                .lineTo(14 * CROSSHAIR_SCALE, 0)
                                .moveTo(0, -14 * CROSSHAIR_SCALE)
                                .lineTo(0, 14 * CROSSHAIR_SCALE)
                                .stroke({
                                    color: CROSSHAIR_DEFAULT_TINT,
                                    width: 2 * CROSSHAIR_SCALE,
                                });
                        }
                    }
                });

                // seed: show up to TARGET_COUNT initially, cycling keywords in order
                const initialToSpawn = Math.min(
                    TARGET_COUNT,
                    keywordsRef.current.length || TARGET_COUNT
                );
                for (let i = 0; i < initialToSpawn; i += 1) {
                    const r = i % ROWS;
                    const kw = getNextKeyword();
                    spawnTarget(app, targetTex, r, kw);
                }
                setLoadingGame(false);

                const update = () => {
                    const cross = crosshairRef.current;
                    if (cross) {
                        cross.clear();
                        cross
                            .circle(0, 0, 8 * CROSSHAIR_SCALE)
                            .stroke({
                                color: CROSSHAIR_DEFAULT_TINT,
                                width: 2 * CROSSHAIR_SCALE,
                            })
                            .moveTo(-14 * CROSSHAIR_SCALE, 0)
                            .lineTo(14 * CROSSHAIR_SCALE, 0)
                            .moveTo(0, -14 * CROSSHAIR_SCALE)
                            .lineTo(0, 14 * CROSSHAIR_SCALE)
                            .stroke({
                                color: CROSSHAIR_DEFAULT_TINT,
                                width: 2 * CROSSHAIR_SCALE,
                            });
                    }

                    let aimed = false;
                    let aimedKeyword: string | null = null;
                    if (!app) return; // type guard
                    for (const t of targetsRef.current) {
                        if (!t.alive) continue;
                        const w = app.screen.width;
                        t.container.x += t.vx;
                        if (t.container.x > w + 100) t.vx = -Math.abs(t.vx);
                        if (t.container.x < -100) t.vx = Math.abs(t.vx);
                        if (cross) {
                            const dx = cross.x - t.container.x;
                            const dy = cross.y - t.container.y;
                            if (dx * dx + dy * dy <= HIT_RADIUS * HIT_RADIUS) {
                                aimed = true;
                                aimedKeyword = t.keyword;
                            }
                        }
                    }
                    if (cross && aimed) {
                        cross.clear();
                        cross
                            .circle(0, 0, 8 * CROSSHAIR_SCALE)
                            .stroke({
                                color: CROSSHAIR_AIM_TINT,
                                width: 2 * CROSSHAIR_SCALE,
                            })
                            .moveTo(-14 * CROSSHAIR_SCALE, 0)
                            .lineTo(14 * CROSSHAIR_SCALE, 0)
                            .moveTo(0, -14 * CROSSHAIR_SCALE)
                            .lineTo(0, 14 * CROSSHAIR_SCALE)
                            .stroke({
                                color: CROSSHAIR_AIM_TINT,
                                width: 2 * CROSSHAIR_SCALE,
                            });
                    }
                    // update aimed description in React state (throttled by change)
                    if (aimed && aimedKeyword) {
                        const found = keywordsRef.current.find(
                            k => k.key === aimedKeyword
                        );
                        const desc = found?.description ?? null;
                        if (desc !== lastDescRef.current) {
                            lastDescRef.current = desc;
                            setAimedDesc(desc);
                        }
                    } else {
                        if (lastDescRef.current !== null) {
                            lastDescRef.current = null;
                            setAimedDesc(null);
                        }
                    }

                    // recycle
                    for (const t of targetsRef.current) {
                        if (!t.alive && poolRef.current.length < 10) {
                            poolRef.current.push(t);
                        }
                    }
                    targetsRef.current = targetsRef.current.filter(
                        t => t.alive
                    );
                };

                app.ticker.add(update);

                // per-row timed spawn to avoid clustering
                const baseIntervalMs = 1200; // base interval
                const jitterMs = 500; // random jitter per tick
                for (let r = 0; r < ROWS; r += 1) {
                    const initialDelay = 200 + Math.floor(Math.random() * 400);
                    const timerId = window.setTimeout(function schedule() {
                        if (destroyed || !app) return;
                        // only spawn if under capacity
                        if (targetsRef.current.length < TARGET_COUNT) {
                            spawnTarget(app, targetTex, r);
                        }
                        const next =
                            baseIntervalMs +
                            Math.floor(Math.random() * jitterMs);
                        spawnTimersRef.current[r] = window.setTimeout(
                            schedule,
                            next
                        ) as unknown as number;
                    }, initialDelay) as unknown as number;
                    spawnTimersRef.current[r] = timerId;
                }
            } catch {
                // fail-safe cleanup on init failure
                if (app) {
                    try {
                        app.destroy();
                    } catch {}
                }
                app = null;
            }
        })();

        return () => {
            destroyed = true;
            if (resizeObserver) {
                try {
                    resizeObserver.disconnect();
                } catch {}
            }
            if (app) {
                try {
                    app.destroy();
                } catch {}
            }
            app = null;
            // clear row timers
            for (const id of spawnTimersRef.current) {
                if (id) {
                    try {
                        window.clearTimeout(id as unknown as number);
                    } catch {}
                }
            }
            spawnTimersRef.current = [];
        };
    }, [spawnTarget, loadingKeywords]);

    const handleNext = () => {
        if (selected.length !== MAX_SELECTION) return;
        const searchNames = selected
            .map(
                k =>
                    keywordsRef.current.find(x => x.key === k)?.displayName || k
            )
            .join(',');
        const search = new URLSearchParams({
            mode: 'aim',
            game,
            leagueId,
            step: 'teams',
            keywords: selected.join(','),
            keywordNames: searchNames,
        });
        router.push(`/pick-and-aim/select?${search.toString()}`);
    };

    useEffect(() => {
        let aborted = false;
        (async () => {
            try {
                setLoadingKeywords(true);
                const res = await fetch('/next-api/pick-aim/keywords', {
                    cache: 'no-store',
                    credentials: 'include',
                });
                if (!res.ok) return;
                const data: Keyword[] = await res.json();
                if (!aborted && Array.isArray(data)) {
                    setKeywords(data);
                    keywordsRef.current = data;
                }
            } catch {
            } finally {
                setLoadingKeywords(false);
            }
        })();
        return () => {
            aborted = true;
        };
    }, []);

    return (
        <div className="pickAimShotKeyword">
            <header className="bar">
                <button className="back" onClick={() => router.back()}>
                    <Image
                        src={resourceBack}
                        alt="back"
                        width={64}
                        height={64}
                    />
                </button>
                <h1>선호하는 키워드를 사격해주세요!</h1>
                <span className="mode">사격 : 키워드 선택</span>
            </header>
            <div className="panel">
                <div className="hud">
                    <div className="ammoWrap">
                        <div className="ammo">탄: {ammo}발</div>
                        {ammo !== MAX_AMMO && (
                            <button
                                className="return"
                                onClick={() => {
                                    // reset keywords and ammo
                                    selectedRef.current = [];
                                    setSelected([]);
                                    setAmmo(MAX_AMMO);
                                    ammoRef.current = MAX_AMMO;
                                }}
                            >
                                <Image
                                    src={resourceReturn}
                                    alt="return"
                                    width={24}
                                    height={24}
                                />
                            </button>
                        )}
                    </div>
                    <div className="aimCenter">
                        {aimedDesc ? (
                            <div className="aimDesc">{aimedDesc}</div>
                        ) : (
                            <div
                                className="aimDesc"
                                style={{ visibility: 'hidden' }}
                            >
                                .
                            </div>
                        )}
                    </div>
                    <div className="selected">
                        선택 키워드 {selected.length} / {MAX_SELECTION}
                        <div className="selectedList">{selectedListText}</div>
                    </div>
                </div>
                <div className="canvasWrap" ref={mountRef} />
                {(loadingKeywords || loadingGame) && (
                    <div className="loadingOverlay">
                        <div className="loadingWrap">
                            <LoadingIcon />
                            <div className="loadingText">
                                {loadingKeywords
                                    ? '키워드를 불러오는 중입니다.'
                                    : '게임을 준비하는 중입니다.'}
                            </div>
                        </div>
                    </div>
                )}
                {keywords.length === 0 && !loadingKeywords && (
                    <div className="no-keywords">키워드 없음</div>
                )}
            </div>
            <div className="footer">
                <TextButton
                    className={`start ${selected.length === MAX_SELECTION ? 'primary' : 'secondary'}`}
                    text="다음"
                    func={handleNext}
                />
            </div>
        </div>
    );
}
