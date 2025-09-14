export interface ValorantStats {
    playerId: number;
    round: number;
    acs: number;
    adr: number;
    apr: number;
    fkpr: number;
    fdpr: number;
    hs: number;
    cl: number;
    kda: number;
}

export interface Player {
    id: number;
    teamId: number;
    teamName: string;
    gameId: number;
    name: string;
    handle: string;
    country: string;
    imgUrl: string;
    valorantStats: ValorantStats;
}