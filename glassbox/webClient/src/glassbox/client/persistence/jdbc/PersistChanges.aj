package glassbox.client.persistence.jdbc;

import org.springframework.jdbc.object.SqlUpdate;

public aspect PersistChanges {
    private BackupDaemon backupDaemon;
    
    pointcut sqlUpdate() : 
        within(glassbox.client.persistence.jdbc..*) && within(SqlUpdate+) && execution(* run(..));
    
    after() returning: sqlUpdate() {
        backupDaemon.setDirty(true);
    }
    
    public BackupDaemon getBackupDaemon() {
        return backupDaemon;
    }

    public void setBackupDaemon(BackupDaemon backupDaemon) {
        this.backupDaemon = backupDaemon;
    }

}
