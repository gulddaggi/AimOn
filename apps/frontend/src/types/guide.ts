export type Player = {
    url: string;
    name: string;
    handle: string;
    country: string;
}

export type GuideResponse = {
    outline?: string;
    keys?: string[];
    teamIcon?: string;
    players?: Player[];
}