import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Game, League } from '@/types/league-and-game';

type LeagueSimple = Omit<League, 'gameId'>;
interface GameSelection {
    selectedGame: Game | null;
    selectedLeague: LeagueSimple | null;
}

const initialState: GameSelection = {
    selectedGame: null,
    selectedLeague: null,
};

export const gameSelectionSlice = createSlice({
    name: 'gameSelection',
    initialState,
    reducers: {
        setSelectedGame: (state, action: PayloadAction<Game>) => {
            state.selectedGame = action.payload;
        },
        setSelectedLeague: (state, action: PayloadAction<LeagueSimple>) => {
            state.selectedLeague = action.payload;
        },
    },
});

export const { setSelectedGame, setSelectedLeague } =
    gameSelectionSlice.actions;
export default gameSelectionSlice.reducer;
