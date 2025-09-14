export interface Match {
    id: number;
    teamId: number;
    opTeam: string;
    gameId: number;
    leagueId: number;
    matchDate: string;
    myScore: number;
    opScore: number;
    isPlayed: boolean;
    vlrMatchId: number | null;
}

export interface MatchWithTeam extends Omit<Match, 'matchDate'> {
    teamName: string;
    matchDate: Date;
    teamIcon: string;
    opTeamIcon: string;
}
