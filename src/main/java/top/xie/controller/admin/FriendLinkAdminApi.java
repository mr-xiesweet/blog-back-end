package top.xie.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.pojo.FriendLink;
import top.xie.response.ResponseResult;
import top.xie.services.IFriendLinkService;

@RequestMapping("/admin/friend_link")
@RestController
public class FriendLinkAdminApi {

    @Autowired
    private IFriendLinkService friendLinkService;

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PostMapping
    public ResponseResult addFriendLink(@RequestBody FriendLink friendLink){

        return friendLinkService.addFriendLink(friendLink);
    }

    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{friendLinkId}")
    public ResponseResult deleteFriendLink(@PathVariable("friendLinkId") String friendLinkId){

        return friendLinkService.deleteFriendLink(friendLinkId);
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/{friendLinkId}")
    public ResponseResult updateFriendLink(@PathVariable("friendLinkId") String friendLinkId,
                                           @RequestBody FriendLink friendLink){

        return friendLinkService.updateFriendLink(friendLinkId, friendLink);
    }

    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/{friendLinkId}")
    public ResponseResult getFriendLink(@PathVariable("friendLinkId") String friendLinkId){

        return friendLinkService.getFriendLink(friendLinkId);
    }

    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list")
    public ResponseResult listFriendLinks(){

        return friendLinkService.listFriendLinks();
    }
}
