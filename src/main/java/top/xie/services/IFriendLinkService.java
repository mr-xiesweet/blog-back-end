package top.xie.services;

import top.xie.pojo.FriendLink;
import top.xie.response.ResponseResult;

public interface IFriendLinkService {

    ResponseResult addFriendLink(FriendLink friendLink);

    ResponseResult getFriendLink(String friendLinkId);

    ResponseResult listFriendLinks();

    ResponseResult deleteFriendLink(String friendLinkId);

    ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink);
}
