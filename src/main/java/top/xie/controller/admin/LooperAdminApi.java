package top.xie.controller.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.xie.interceptor.CheckTooFrequentCommit;
import top.xie.pojo.Looper;
import top.xie.response.ResponseResult;
import top.xie.services.ILoopService;

@RequestMapping("/admin/loop")
@RestController
public class LooperAdminApi {


    @Autowired
    private ILoopService loopService;

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PostMapping
    public ResponseResult addLoop(@RequestBody Looper looper){

        return loopService.addLoop(looper);
    }

    @PreAuthorize("@permission.adminPermission()")
    @DeleteMapping("/{loopId}")
    public ResponseResult deleteLoop(@PathVariable("loopId") String loopId){

        return loopService.deleteLoop(loopId);
    }

    @CheckTooFrequentCommit
    @PreAuthorize("@permission.adminPermission()")
    @PutMapping("/{loopId}")
    public ResponseResult updateLoop(@PathVariable("loopId") String loopId,
                                     @RequestBody Looper looper) {
        return loopService.updateLoop(loopId,looper);
    }

    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/{loopId}")
    public ResponseResult getLoop(@PathVariable("loopId") String loopId){

        return loopService.getLoop(loopId);
    }
    @PreAuthorize("@permission.adminPermission()")
    @GetMapping("/list")
    public ResponseResult listLoops(){
        return loopService.listLoops();
    }
}
