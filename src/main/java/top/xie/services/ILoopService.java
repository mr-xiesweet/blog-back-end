package top.xie.services;

import top.xie.pojo.Looper;
import top.xie.response.ResponseResult;

public interface ILoopService {
    ResponseResult addLoop(Looper looper);

    ResponseResult getLoop(String loopId);

    ResponseResult listLoops();

    ResponseResult updateLoop(String loopId, Looper looper);

    ResponseResult deleteLoop(String loopId);
}
