'use client';

import { useEffect, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';
import { likesTeamFetch } from '@/lib/redux/feature/teams/likes-team-slice';
import LeagueGameSelector from '@/components/organisms/league-game-selector';
import Chatbot from '@/components/organisms/chatbot';
import LogoutToast from '../molecules/auth/logout-toast';
import LoginToast from '../molecules/auth/login-toast';
import SignInToast from '../molecules/auth/sign-in-toast';
import RouteToast from '../molecules/route-toast';
import { Game, League } from '@/types/league-and-game';
import { Team } from '@/types/team';
import { Match, MatchWithTeam } from '@/types/match';
import { gameApi } from '@/lib/axios/game/game-api';
import { leagueApi } from '@/lib/axios/league/league-api';
import {
    teamFetchByLeagueApi,
    teamFetchByName,
} from '@/lib/axios/teams/teams-api';
import FavTeamArea from '../organisms/fav-team-area';
import Dashboard from '../organisms/dashboard';
import { matchFetchByTeamApi } from '@/lib/axios/match/match-api';
import { playerFetchByTeamApi } from '@/lib/axios/player/player-api';
import { Player } from '@/types/player';

export default function HomeTemplate() {
    const dispatch = useAppDispatch();
    const isAuthorized = useAppSelector(state => state.auth.isAuthorized);
    const likedTeamList = useAppSelector(state => state.likesTeam.likesTeams);
    const likesTeamStatus = useAppSelector(state => state.likesTeam.status);
    const selectedLeague = useAppSelector(
        state => state.gameSelection.selectedLeague?.id
    );

    const [teamList, setTeamList] = useState<Team[]>([]);
    const [playerList, setPlayerList] = useState<Player[]>([]);
    const [selectedTeamPlayers, setSelectedTeamPlayers] = useState<Player[]>(
        []
    );
    const [teamMatches, setTeamMatches] = useState<
        Map<number, MatchWithTeam[]>
    >(new Map());
    const [prevMatch, setPrevMatch] = useState<MatchWithTeam[]>([]);
    const [nextMatch, setNextMatch] = useState<MatchWithTeam[]>([]);
    const [isLoadingMatches, setIsLoadingMatches] = useState(false);
    const [selectedTeamId, setSelectedTeamId] = useState<number>(0);
    const [games, setGames] = useState<Game[]>([]);
    const [leagues, setLeagues] = useState<League[]>([]);

    async function preLoading() {
        const gameRes = await gameApi();
        const fetchedGames: Game[] = gameRes?.data || [];
        setGames(fetchedGames);

        const fetchedLeagues: League[] = [];
        for (const game of fetchedGames) {
            const leagueRes = await leagueApi(game.id);
            fetchedLeagues.push(...(leagueRes?.data || []));
        }
        setLeagues(fetchedLeagues);
    }

    useEffect(() => {
        preLoading();
        setPrevMatch([]);
        setNextMatch([]);
    }, []);

    useEffect(() => {
        if (isAuthorized) {
            dispatch(likesTeamFetch());
        }
    }, [isAuthorized, dispatch]);

    const processTeamMatches = async (
        team: Team,
        teamCache: Map<string, Team[] | null>
    ): Promise<MatchWithTeam[]> => {
        const matchRes = await matchFetchByTeamApi(team.id);
        const matches = matchRes?.data || [];

        const uniqueOpTeams: string[] = [
            ...new Set(matches.map((match: Match) => match.opTeam)),
        ] as string[];

        const teamsToFetch: string[] = uniqueOpTeams.filter(
            (teamName: string) => !teamCache.has(teamName)
        );

        if (teamsToFetch.length > 0) {
            const opTeamPromises = teamsToFetch.map((teamName: string) =>
                teamFetchByName(teamName).catch(() => null)
            );
            const opTeamResults = await Promise.all(opTeamPromises);

            teamsToFetch.forEach((teamName: string, index: number) => {
                const result = opTeamResults[index];
                teamCache.set(teamName, result?.data || null);
            });
        }

        const teamMatches = matches.map((match: Match) => {
            const opTeamData = teamCache.get(match.opTeam);
            return {
                ...match,
                matchDate: new Date(match.matchDate),
                teamName: team.teamName,
                teamIcon: team.imgUrl,
                opTeamIcon: opTeamData?.[0]?.imgUrl || '',
            } as MatchWithTeam;
        });

        return teamMatches;
    };

    const loadLikedTeamMatches = async () => {
        setIsLoadingMatches(true);
        try {
            const teamCache = new Map<string, Team[] | null>();
            const newTeamMatches = new Map<number, MatchWithTeam[]>();

            for (const team of likedTeamList) {
                const teamMatchResult = await processTeamMatches(
                    team,
                    teamCache
                );
                teamMatchResult.sort(
                    (a, b) => b.matchDate.getTime() - a.matchDate.getTime()
                );
                newTeamMatches.set(team.id, teamMatchResult);
            }

            setTeamMatches(newTeamMatches);

            const firstTeamMatches =
                newTeamMatches.get(likedTeamList[0].id) || [];
            const prevMatch = firstTeamMatches
                .filter(match => match.isPlayed)
                .slice(0, 3);
            const nextMatch = firstTeamMatches
                .filter(match => !match.isPlayed)
                .slice(0, 3);

            setPrevMatch(prevMatch);
            setNextMatch(nextMatch);
        } finally {
            setIsLoadingMatches(false);
        }
    };

    const loadDefaultTeamMatches = async () => {
        if (!selectedLeague) return;

        setIsLoadingMatches(true);
        try {
            const teamCache = new Map<string, Team[] | null>();
            const teamFetchRes = await teamFetchByLeagueApi(selectedLeague);
            const teamList = teamFetchRes?.data.slice(0, 4);

            const teamMatchPromises = teamList.map((team: Team) =>
                processTeamMatches(team, teamCache)
            );
            const teamMatchResults = await Promise.all(teamMatchPromises);
            const allMatches = teamMatchResults.flat();

            allMatches.sort(
                (a, b) => b.matchDate.getTime() - a.matchDate.getTime()
            );

            const prevMatch = allMatches
                .filter(match => match.isPlayed)
                .slice(0, 3);
            const nextMatch = allMatches
                .filter(match => !match.isPlayed)
                .slice(0, 3);

            const teamPlayerPromises = teamList.map((team: Team) =>
                playerFetchByTeamApi(team.id).catch(() => null)
            );
            const teamPlayerResults = await Promise.all(teamPlayerPromises);
            const allPlayers = teamPlayerResults.flatMap((result, index) => {
                if (result?.data) {
                    return result.data.map((player: Player) => ({
                        ...player,
                        teamName: teamList[index].teamName,
                    }));
                }
                return [];
            });

            // 모든 데이터가 준비된 후 한번에 state 업데이트
            setTeamList(teamList);
            setPrevMatch(prevMatch);
            setNextMatch(nextMatch);
            setPlayerList(allPlayers);
        } finally {
            setIsLoadingMatches(false);
        }
    };

    useEffect(() => {
        if (likesTeamStatus === 'success' && likedTeamList.length > 0) {
            loadLikedTeamMatches();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [likesTeamStatus, likedTeamList.length]);

    useEffect(() => {
        if (
            (!isAuthorized ||
                (likesTeamStatus === 'success' &&
                    likedTeamList.length === 0)) &&
            selectedLeague
        ) {
            loadDefaultTeamMatches();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isAuthorized, likesTeamStatus, likedTeamList.length, selectedLeague]);

    useEffect(() => {
        if (selectedTeamId > 0 && teamMatches.has(selectedTeamId)) {
            const selectedTeamMatches = teamMatches.get(selectedTeamId) || [];
            const prevMatch = selectedTeamMatches
                .filter(match => match.isPlayed)
                .slice(0, 3);
            const nextMatch = selectedTeamMatches
                .filter(match => !match.isPlayed)
                .slice(0, 3);

            setPrevMatch(prevMatch);
            setNextMatch(nextMatch);
        }
    }, [selectedTeamId, teamMatches]);

    useEffect(() => {
        const loadSelectedTeamPlayers = async () => {
            if (selectedTeamId > 0) {
                try {
                    const selectedTeam = likedTeamList.find(
                        (team: Team) => team.id === selectedTeamId
                    );
                    const playersRes =
                        await playerFetchByTeamApi(selectedTeamId);
                    if (playersRes?.data && selectedTeam) {
                        const playersWithTeamName = playersRes.data.map(
                            (player: Player) => ({
                                ...player,
                                teamName: selectedTeam.teamName,
                            })
                        );
                        setSelectedTeamPlayers(playersWithTeamName);
                    }
                } catch (error) {
                    console.error('선수 정보 로딩 실패:', error);
                    setSelectedTeamPlayers([]);
                }
            }
        };

        loadSelectedTeamPlayers();
    }, [selectedTeamId, likedTeamList]);

    return (
        <>
            <RouteToast />
            <LoginToast />
            <SignInToast />
            <LogoutToast />
            <div className="homeTemplate">
                <div className="hero">
                    <div className="heroBackdrop" />
                    <div className="heroOverlay" />
                    <div className="heroInner">
                        <div className="heroTitle">AimOn</div>
                        <div className="heroSubtitle">
                            FPS e스포츠 팬덤의 시작, 이 곳에서
                        </div>
                    </div>
                </div>
                <Chatbot />
                {likedTeamList.length > 0 ? null : (
                    <LeagueGameSelector game={games} league={leagues} />
                )}
                <div className="cards">
                    <div className="card cardPrimary">
                        <Dashboard
                            teams={
                                likedTeamList.length > 0
                                    ? likedTeamList
                                    : teamList
                            }
                            players={
                                likedTeamList.length > 0
                                    ? selectedTeamPlayers
                                    : playerList
                            }
                        />
                    </div>
                    <div className="card cardAlt">
                        <FavTeamArea
                            hasLikedTeam={likedTeamList.length > 0}
                            prevMatch={prevMatch}
                            nextMatch={nextMatch}
                            isLoading={isLoadingMatches}
                            selectedTeamId={selectedTeamId}
                            setSelectedTeamId={setSelectedTeamId}
                        />
                    </div>
                </div>
            </div>
        </>
    );
}
