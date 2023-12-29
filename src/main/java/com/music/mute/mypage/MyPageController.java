package com.music.mute.mypage;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.music.mute.login.MemberVO;

import lombok.extern.log4j.Log4j;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.follow.UnfollowPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.ChangePlaylistsDetailsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

@Controller
@Log4j
public class MyPageController {
	
	@Autowired
	private SpotifyApi spotifyApi;
	
	@Autowired
	private MyPageService mypageService;
	
	@GetMapping("/mypage")
	public String getUserPlaylists(Model model, HttpSession session) {
		// 사용자의 Access Token을 세션에서 가져옴
		String accessToken = (String) session.getAttribute("accessToken");
		String userid=(String)session.getAttribute("spotifyUserId");
		MemberVO user=mypageService.mypageNickName(userid);
		if (accessToken != null && user!=null) {
			try {
				spotifyApi.setAccessToken(accessToken);
				final GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi
						.getListOfCurrentUsersPlaylists().build();

				final CompletableFuture<Paging<PlaylistSimplified>> playlistsFuture = playlistsRequest.executeAsync();

				PlaylistSimplified[] playlists = playlistsFuture.join().getItems();
				log.info("playlists="+playlists);
				model.addAttribute("playlists", playlists);
				model.addAttribute("nickName",user.getS_NAME());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// Access Token이 없는 경우, 로그인 페이지로 리다이렉트 또는 에러 처리
			return "redirect:/login"; // 예시: 로그인 페이지로 리다이렉트
		}
		return "/mypage";
	}//getUserPlaylists-----------------------------------------
	
	//mypage 플리 이름 수정 ==> Ambiguous mapping에러 발생, apiplaylist 랑 이름 겹쳐서 변경함
	@PostMapping("/updatePlaylistmy")
	public String updatePlaylist(Model model, HttpSession session, @RequestParam String playlistId, @RequestParam String editPlaylistName) {
	    String accessToken = (String) session.getAttribute("accessToken");

	    if (accessToken != null) {
	        try {
	            spotifyApi.setAccessToken(accessToken);

	            // Spotify API를 사용하여 플레이리스트의 이름을 변경
	            final ChangePlaylistsDetailsRequest changePlaylistDetailsRequest = spotifyApi
	                    .changePlaylistsDetails(playlistId)
	                    .name(editPlaylistName)
	                    .build();

	            changePlaylistDetailsRequest.execute();

	            // 수정 후, 사용자에게 적절한 메시지를 전달
	            model.addAttribute("message", "Playlist updated successfully");
	        } catch (Exception e) {
	            e.printStackTrace();
	            model.addAttribute("error", "Error updating playlist");
	        }
	    } else {
	        return "redirect:/login";
	    }

	    return "redirect:/mypage";
	}//updatePlaylist----------------------------------------------
	
	@DeleteMapping("/deletePlaylistmy")
	public ResponseEntity<String> deletePlaylist(@RequestParam String playlistId, HttpSession session) {
		String accessToken = (String) session.getAttribute("accessToken");

		if (accessToken != null) {
			try {
				spotifyApi.setAccessToken(accessToken);

				// 플레이리스트 언팔로우 API 요청
				final UnfollowPlaylistRequest unfollowPlaylistRequest = spotifyApi.unfollowPlaylist(playlistId).build();
				unfollowPlaylistRequest.execute();

				// 삭제 후, 적절한 응답 반환
				return ResponseEntity.ok("Playlist delete successfully");
			} catch (Exception e) {
				e.printStackTrace();
				// 에러가 발생하면 500 Internal Server Error 반환
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting playlist");
			}
		} else {
			// 사용자가 인증되지 않은 경우 401 Unauthorized 반환
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}
	}//deletePlaylist----------------------------------------------
	
	@RequestMapping(value = "/updateNickNameJson", method = RequestMethod.POST)
	@ResponseBody
    public ModelMap updateNickname(@RequestParam String nickName, HttpSession session, Model model) {
        String userId = (String) session.getAttribute("spotifyUserId");

     // 닉네임 업데이트 쿼리 실행
        MemberVO member = new MemberVO();
        member.setS_ID(userId);
        member.setS_NAME(nickName);
        mypageService.updateNickname(member);
        ModelMap map=new ModelMap();
        map.put("result", "success");
        // 예시: 사용 가능한 닉네임인 경우 업데이트 후 main 페이지로 리다이렉트
        return map;
    }
}