package cn.zhiskey.sfs.peer;

/**
 * 节点的状态
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum PeerStatus {
    /**
     * 节点刚刚启动
     */
    START,

    /**
     * 节点正在尝试连接种子节点，并等待种子节点的hashID
     */
    WAIT_SEED_HASH_ID,

    /**
     * 节点正在运行状态
     */
    RUNNING,
}
