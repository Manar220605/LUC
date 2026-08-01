import NextAuth from 'next-auth';
import Keycloak from 'next-auth/providers/keycloak';
import type { JWT } from 'next-auth/jwt';

const issuer = process.env.KEYCLOAK_ISSUER!;
const internalUrl = process.env.KEYCLOAK_INTERNAL_URL?.replace(/\/$/, '');

function keycloakTokenEndpoint(): string {
  const realmPath = new URL(issuer).pathname;
  const publicBase = issuer.slice(0, issuer.length - realmPath.length);
  const base = internalUrl ?? publicBase;
  return `${base}${realmPath}/protocol/openid-connect/token`;
}

function keycloakProvider() {
  const config = {
    clientId: process.env.KEYCLOAK_CLIENT_ID!,
    clientSecret: process.env.KEYCLOAK_CLIENT_SECRET ?? '',
    issuer,
  };

  if (!internalUrl) {
    return Keycloak(config);
  }

  const realmPath = new URL(issuer).pathname;
  const publicBase = issuer.slice(0, issuer.length - realmPath.length);

  return Keycloak({
    ...config,
    authorization: `${publicBase}${realmPath}/protocol/openid-connect/auth`,
    token: `${internalUrl}${realmPath}/protocol/openid-connect/token`,
    userinfo: `${internalUrl}${realmPath}/protocol/openid-connect/userinfo`,
  });
}

async function refreshAccessToken(token: JWT): Promise<JWT> {
  try {
    const response = await fetch(keycloakTokenEndpoint(), {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        client_id: process.env.KEYCLOAK_CLIENT_ID!,
        grant_type: 'refresh_token',
        refresh_token: token.refreshToken!,
      }),
    });

    const refreshed = await response.json();

    if (!response.ok) {
      throw new Error(refreshed.error_description ?? refreshed.error ?? 'Refresh failed');
    }

    return {
      ...token,
      accessToken: refreshed.access_token,
      accessTokenExpires: Date.now() + refreshed.expires_in * 1000,
      refreshToken: refreshed.refresh_token ?? token.refreshToken,
      error: undefined,
    };
  } catch (error) {
    console.error('Failed to refresh access token', error);
    return {
      ...token,
      accessToken: undefined,
      refreshToken: undefined,
      accessTokenExpires: undefined,
      error: 'RefreshAccessTokenError',
    };
  }
}

export const { handlers, signIn, signOut, auth } = NextAuth({
  trustHost: true,
  providers: [keycloakProvider()],
  callbacks: {
    async jwt({ token, account }) {
      if (account) {
        return {
          ...token,
          accessToken: account.access_token,
          refreshToken: account.refresh_token,
          accessTokenExpires: account.expires_at
            ? account.expires_at * 1000
            : Date.now() + (account.expires_in ?? 300) * 1000,
          idToken: account.id_token,
        };
      }

      if (token.error === 'RefreshAccessTokenError') {
        return token;
      }

      if (token.accessTokenExpires && Date.now() < token.accessTokenExpires) {
        return token;
      }

      if (!token.refreshToken) {
        return { ...token, error: 'RefreshAccessTokenError' };
      }

      return refreshAccessToken(token);
    },
    async session({ session, token }) {
      if (token.error) {
        session.error = token.error;
        return session;
      }
      session.accessToken = token.accessToken;
      return session;
    },
  },
});
