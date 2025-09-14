export interface Team {
    id: number;
    gameId: number;
    leagueId: number;
    teamName: string;
    country: string;
    winRate: number;
    attackWinRate: number;
    defenseWinRate: number;
    imgUrl: string;
    point: number;
}
