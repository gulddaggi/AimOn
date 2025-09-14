import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { Message } from '@/types/message';
import { chatbotApi } from '@/lib/axios/ai/chatbot-api';
import { guideApi } from '@/lib/axios/ai/guide-api';

interface Chat {
    messages: Message[];
    isLoading: boolean;
    isSucceeded: 'success' | 'fail' | undefined;
}

const initialState: Chat = {
    messages: [],
    isLoading: false,
    isSucceeded: undefined,
};

export const chatbot = createAsyncThunk('/ask', async (question: string) => {
    const res = await chatbotApi(question);
    if (res?.status === 200) return res?.data;
    throw new Error('Chatbot failed');
});

export const fetchGuide = createAsyncThunk('guide/fetch', async (elements: string) => {
    const res = await guideApi(elements);
    if (res?.status === 200) return res?.data;
    throw new Error('Guide fetch failed');
});

export const chatSlice = createSlice({
    name: 'chat',
    initialState,
    reducers: {
        addMessage: (state, action: PayloadAction<Message>) => {
            state.messages.push(action.payload);
        },
        resetMessages: state => {
            state.messages = [];
        },
        setIsLoading: (state, action: PayloadAction<boolean>) => {
            state.isLoading = action.payload;
        },
    },
    extraReducers(builder) {
        builder
            .addCase(chatbot.pending, state => {
                state.isLoading = true;
                state.isSucceeded = undefined;
            })
            .addCase(chatbot.fulfilled, (state, action) => {
                state.isLoading = false;
                state.messages.push({
                    context: action.payload.answer,
                    sender: 'chatbot',
                });
                state.isSucceeded = 'success';
            })
            .addCase(chatbot.rejected, state => {
                state.isLoading = false;
                state.messages.push({
                    context:
                        '예기지 못한 오류가 발생했습니다. 다시 시도해주세요.',
                    sender: 'chatbot',
                });
                state.isSucceeded = 'fail';
            })
            .addCase(fetchGuide.pending, state => {
                state.isLoading = true;
            })
            .addCase(fetchGuide.fulfilled, (state, action) => {
                state.isLoading = false;
                state.messages.push({
                    context: action.payload,
                    sender: 'guide',
                });
            })
            .addCase(fetchGuide.rejected, state => {
                state.isLoading = false;
                state.messages.push({
                    context: '가이드를 불러오는 중 오류가 발생했습니다.',
                    sender: 'guide',
                });
            });
    },
});

export const { addMessage, resetMessages, setIsLoading } = chatSlice.actions;
export default chatSlice.reducer;
