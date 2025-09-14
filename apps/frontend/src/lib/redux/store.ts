import { configureStore, combineReducers } from '@reduxjs/toolkit';
import sessionStorage from 'redux-persist/lib/storage/session';
import {
    persistReducer,
    FLUSH,
    REHYDRATE,
    PAUSE,
    PERSIST,
    PURGE,
    REGISTER,
} from 'redux-persist';
import inputTextReducer from './feature/input/input-text-slice';
import inputEmailReducer from './feature/input/input-email-slice';
import inputPasswordReducer from './feature/input/input-password-slice';
import gameSelectionReducer from './feature/game-selection/game-selection-slice';
import chatReducer from './feature/chatbot/chat-slice';
import authReducer from './feature/auth/auth-slice';
import likesTeamReducer from './feature/teams/likes-team-slice';

const persistConfig = {
    key: 'root',
    storage: sessionStorage,
    whitelist: ['likesTeam'],
};

const authPersistConfig = {
    key: 'auth',
    storage: sessionStorage,
    whitelist: ['isAuthorized'],
};

const rootReducer = combineReducers({
    auth: persistReducer(authPersistConfig, authReducer),
    inputText: inputTextReducer,
    inputEmail: inputEmailReducer,
    inputPassword: inputPasswordReducer,
    gameSelection: gameSelectionReducer,
    chat: chatReducer,
    likesTeam: likesTeamReducer,
});

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const makeStore = () => {
    return configureStore({
        reducer: persistedReducer,
        middleware: getDefaultMiddleware =>
            getDefaultMiddleware({
                serializableCheck: {
                    ignoredActions: [
                        FLUSH,
                        REHYDRATE,
                        PAUSE,
                        PERSIST,
                        PURGE,
                        REGISTER,
                    ],
                },
            }),
    });
};

export type AppStore = ReturnType<typeof makeStore>;
// RootState는 persistReducer로 인한 PersistPartial 문제를 피하기 위해 원본 rootReducer 기반으로 정의
export type RootState = ReturnType<typeof rootReducer>;
export type AppDispatch = AppStore['dispatch'];
