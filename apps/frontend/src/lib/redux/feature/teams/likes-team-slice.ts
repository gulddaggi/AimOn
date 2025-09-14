import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import { Team } from '@/types/team';
import { likesTeamFetchApi } from '@/lib/axios/teams/likes-team-api';

type LikesTeamStatus = 'loading' | 'success' | 'failed' | undefined;

interface LikesTeam {
    likesTeams: Team[];
    status: LikesTeamStatus;
}

const initialState: LikesTeam = {
    likesTeams: [],
    status: undefined,
};

export const likesTeamFetch = createAsyncThunk('/likes/teams/me', async () => {
    const res = await likesTeamFetchApi();

    if (res?.status === 200) return res.data;
    throw new Error('Likes team fetch failed');
});

export const likesTeamSlice = createSlice({
    name: 'likesTeamSlice',
    initialState,
    reducers: {
        setLikesTeamList: (state, action: PayloadAction<Team[]>) => {
            state.likesTeams = action.payload;
        },
        addLikesTeam: (state, action: PayloadAction<Team>) => {
            state.likesTeams.push(action.payload);
        },
        removeLikesTeam: (state, action: PayloadAction<number>) => {
            const prevList = state.likesTeams;
            state.likesTeams = prevList
                .map(item => item)
                .filter(item => item.id !== action.payload);
        },
        resetLikesTeamList: state => {
            state.likesTeams = [];
            state.status = undefined;
        },
    },
    extraReducers(builder) {
        builder
            .addCase(likesTeamFetch.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(likesTeamFetch.fulfilled, (state, action) => {
                state.likesTeams = action.payload;
                state.status = 'success';
            })
            .addCase(likesTeamFetch.rejected, (state) => {
                state.status = 'failed';
            });
    },
});

export const { setLikesTeamList, addLikesTeam, removeLikesTeam, resetLikesTeamList } =
    likesTeamSlice.actions;
export default likesTeamSlice.reducer;
