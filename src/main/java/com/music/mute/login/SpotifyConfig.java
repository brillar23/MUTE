package com.music.mute.login;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import se.michaelthelin.spotify.SpotifyApi;

@Configuration
public class SpotifyConfig {

	//뮤테 계정 관련
	/*
	 * private String clientId = "61731dfa4f5a4f81a934c76fe09958d8";
	 * 
	 * private String clientSecret = "7b9fbc7d7cb14319b7af63f5c5f18176";
	 * 
	 * private String redirectUri = "http://localhost:9089/mute/main";
	 */
	 
	//최희정 계정 관련
	 private String clientId = "c82612eab8154313a45a1233cb2e7374";
	 
	 private String clientSecret = "ac3b1b120d9c4f909474e86d723ad055";
	 
	 private String redirectUri = "http://localhost:9089/mute/main";

			
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public SpotifyApi spotifyApi() {
		return new SpotifyApi
				.Builder()
				.setClientId(clientId)
				.setClientSecret(clientSecret)
				.setRedirectUri(URI.create(redirectUri))
				.build();
	}
}
