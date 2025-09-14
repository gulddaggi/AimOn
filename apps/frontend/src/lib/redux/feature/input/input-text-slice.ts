import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface InputText {
    value: string;
    valid: boolean;
}

const initialState: InputText = {
    value: '',
    valid: false,
};

export const inputTextSlice = createSlice({
    name: 'inputText',
    initialState,
    reducers: {
        textUpdate: (state, action: PayloadAction<string>) => {
            state.value = action.payload;
        },
        textValidUpdate: (state, action: PayloadAction<boolean>) => {
            state.valid = action.payload;
        },
    },
});

export const { textUpdate, textValidUpdate } = inputTextSlice.actions;
export default inputTextSlice.reducer;
