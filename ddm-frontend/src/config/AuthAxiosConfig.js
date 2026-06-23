import axios from 'axios';

const ensureTrailingSlash = (value) => value.endsWith('/') ? value : `${value}/`;

const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || '/api/';
const authBaseUrl = ensureTrailingSlash(
    process.env.REACT_APP_AUTH_BASE_URL || `${ensureTrailingSlash(apiBaseUrl)}auth`
);

const authAxiosInstance = axios.create({
    baseURL: authBaseUrl,
    headers: {
        'Content-Type': 'application/json',
    }
});

export default authAxiosInstance;
