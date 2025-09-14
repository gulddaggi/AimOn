'use server';
import { chatbotInstance } from '../axios-instance';
import { ChatbotError } from '../error/chatbot-error';

export const chatbotApi = async (question: string) => {
    try {
        const res = await chatbotInstance({
            url: '/ask',
            method: 'post',
            data: {
                question: question,
            },
        });

        if (res.status === 200) {
            return { status: res.status, data: res.data };
        }
    } catch (error) {
        console.error(error);

        if (error instanceof ChatbotError) return error.response;
    }
};
