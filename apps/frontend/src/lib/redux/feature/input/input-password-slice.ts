import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface InputPassword {
    value: string;
    valid: boolean;
}

const initialState: InputPassword = {
    value: '',
    valid: false,
};

export const inputPasswordSlice = createSlice({
    name: 'inputPassword',
    initialState,
    reducers: {
        passwordUpdate: (state, action: PayloadAction<string>) => {
            state.value = action.payload;
        },
        passwordValidUpdate: (state, action: PayloadAction<boolean>) => {
            state.valid = action.payload;
        },
    },
});

export const { passwordUpdate, passwordValidUpdate } =
    inputPasswordSlice.actions;
export default inputPasswordSlice.reducer;
