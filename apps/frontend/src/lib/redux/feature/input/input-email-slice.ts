import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface InputEmail {
    value: string;
    valid: boolean;
}

const initialState: InputEmail = {
    value: '',
    valid: false,
};

export const inputEmailSlice = createSlice({
    name: 'inputEmail',
    initialState,
    reducers: {
        emailUpdate: (state, action: PayloadAction<string>) => {
            state.value = action.payload;
        },
        emailValidUpdate: (state, action: PayloadAction<boolean>) => {
            state.valid = action.payload;
        },
    },
});

export const { emailUpdate, emailValidUpdate } = inputEmailSlice.actions;
export default inputEmailSlice.reducer;
