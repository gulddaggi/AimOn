import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { loginApi } from '@/lib/axios/auth/login-api';
import { signInApi } from '@/lib/axios/auth/sign-in-api';
import { logoutApi } from '@/lib/axios/auth/logout-api';

type SuccessState = 'login' | 'signIn' | 'logout' | 'fail' | undefined;
interface Auth {
    isAuthorized: boolean;
    isSucceeded: SuccessState;
}

interface LoginCredential {
    email: string;
    password: string;
}

interface SignInCredential {
    nickname: string;
    email: string;
    password: string;
}

const initialState: Auth = {
    isAuthorized: false,
    isSucceeded: undefined,
};

export const login = createAsyncThunk(
    'auth/login',
    async (loginCredential: LoginCredential) => {
        const res = await loginApi(
            loginCredential.email,
            loginCredential.password
        );
        if (res?.status === 200) return res?.data;
        throw new Error('Login failed');
    }
);

export const signIn = createAsyncThunk(
    'auth/join',
    async (signInCredential: SignInCredential) => {
        const res = await signInApi(
            signInCredential.nickname,
            signInCredential.email,
            signInCredential.password
        );
        console.log('signInApi 결과:', res);
        console.log('res?.status:', res?.status);
        if (res?.status === 201) return res?.data;
        throw new Error('Login failed');
    }
);

export const logout = createAsyncThunk('auth/logout', async () => {
    const res = await logoutApi();
    if (res?.status === 200) return res?.data;
    throw new Error('Login failed');
});

export const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setIsAuthorized: (state, action: PayloadAction<boolean>) => {
            state.isAuthorized = action.payload;
        },
        setIsSucceeded: (state, action: PayloadAction<SuccessState>) => {
            state.isSucceeded = action.payload;
        },
    },
    extraReducers(builder) {
        builder
            .addCase(login.fulfilled, state => {
                state.isAuthorized = true;
                state.isSucceeded = 'login';
            })
            .addCase(login.rejected, state => {
                state.isAuthorized = false;
                state.isSucceeded = 'fail';
            })
            .addCase(signIn.fulfilled, state => {
                state.isAuthorized = true;
                state.isSucceeded = 'signIn';
            })
            .addCase(signIn.rejected, state => {
                state.isAuthorized = false;
                state.isSucceeded = 'fail';
            })
            .addCase(logout.fulfilled, state => {
                state.isAuthorized = false;
                state.isSucceeded = 'logout';
            })
            .addCase(logout.rejected, state => {
                state.isAuthorized = true;
                state.isSucceeded = 'fail';
            });
    },
});

export const { setIsAuthorized, setIsSucceeded } = authSlice.actions;
export default authSlice.reducer;
