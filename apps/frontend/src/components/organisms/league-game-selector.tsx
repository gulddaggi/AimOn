'use client';
import { useEffect, useState } from 'react';
import Dropdown from '@/components/molecules/dropdown/dropdown';
import { Options } from '@/types/options';
import { useAppSelector, useAppDispatch } from '@/lib/redux/hooks';
import {
    setSelectedGame,
    setSelectedLeague,
} from '@/lib/redux/feature/game-selection/game-selection-slice';
import { Game, League } from '@/types/league-and-game';
export default function LeagueGameSelector(props: {
    game: Game[] | null;
    league: League[] | null;
}) {
    const gameOptions: Options =
        props.game?.map(item => {
            return { value: item.id, name: item.name };
        }) || [];

    const dispatch = useAppDispatch();
    const gameName = useAppSelector(
        state => state.gameSelection.selectedGame?.name
    );
    const leagueName = useAppSelector(
        state => state.gameSelection.selectedLeague?.name
    );

    const [selectedGameId, setSelectedGameId] = useState<number | null>(null);
    const [leagueOptions, setLeagueOptions] = useState<Options>([]);

    // 선택된 게임 ID가 바뀔 때마다 리그 옵션 업데이트
    useEffect(() => {
        const filteredLeagues =
            props.league?.filter(league => league.gameId === selectedGameId) ||
            [];

        const newLeagueOptions = filteredLeagues.map(item => ({
            value: item.id,
            name: item.name,
        }));

        setLeagueOptions(newLeagueOptions);

        // 새 리그 목록이 있으면 첫 번째 리그 자동 선택
        if (newLeagueOptions.length > 0) {
            const firstLeague = newLeagueOptions[0];
            handleLeagueSelect(firstLeague.value, firstLeague.name);
        }
    }, [selectedGameId, props.league]); // eslint-disable-line react-hooks/exhaustive-deps

    const handleLeagueSelect = (id: number, league: string) => {
        dispatch(setSelectedLeague({ id: id, name: league }));
    };

    const handleGameSelect = (id: number, game: string) => {
        dispatch(setSelectedGame({ id: id, name: game }));
        setSelectedGameId(id); // 즉시 게임 ID 상태 업데이트
    };

    useEffect(() => {
        if (gameOptions.length > 0) {
            handleGameSelect(gameOptions[0].value, gameOptions[0].name);
        }
    }, [props.game]); // eslint-disable-line react-hooks/exhaustive-deps

    return (
        <div className="leagueGameSelector">
            <Dropdown
                className="game"
                text={gameName || '게임 선택'}
                options={gameOptions}
                func={handleGameSelect}
            />
            <Dropdown
                className="league"
                text={leagueName || '리그 선택'}
                options={leagueOptions}
                func={handleLeagueSelect}
            />
        </div>
    );
}
