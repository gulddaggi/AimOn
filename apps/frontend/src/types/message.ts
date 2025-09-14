import { GuideResponse } from './guide';

export type Message = {
    context: string | GuideResponse;
    sender: 'user' | 'chatbot' | 'guide';
};
