package at.newmedialab.lmf.worker.test;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.marmotta.platform.core.api.task.Task;
import org.apache.marmotta.platform.core.api.task.TaskInfo;
import org.apache.marmotta.platform.core.api.task.TaskManagerService;

import javax.enterprise.inject.Alternative;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
* Add file description here!
*
* @author Sebastian Schaffert (sschaffert@apache.org)
*/
@Alternative
class MockTaskManagerService implements TaskManagerService {


    public MockTaskManagerService() {
    }

    @Override
    public Task createTask(String name) {
        return new Task(RandomStringUtils.randomAscii(8)) {
            @Override
            public long endTask() {
                return 0;
            }
        };
    }

    @Override
    public Task createTask(String name, String group) {
        return createTask(name);
    }

    @Override
    public Task createSubTask(String name) {
        return createTask(name);
    }

    @Override
    public Task createSubTask(String name, String group) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Task getTask() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void endTask(TaskInfo task) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TaskInfo> getTasks() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, List<TaskInfo>> getTasksByGroup() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<WeakReference<Thread>, Stack<TaskInfo>> getTasksByThread() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
