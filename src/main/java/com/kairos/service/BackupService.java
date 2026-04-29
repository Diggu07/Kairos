package com.kairos.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupService {

    private static final String BACKUP_DIR = "backups/";

    public BackupService() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String createBackup(String dbFilePath) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"));
        String backupFileName = BACKUP_DIR + "kairos-backup-" + timestamp + ".zip";
        
        try (FileOutputStream fos = new FileOutputStream(backupFileName);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            Path fileToBackup = Paths.get(dbFilePath);
            if (Files.exists(fileToBackup)) {
                ZipEntry zipEntry = new ZipEntry(fileToBackup.getFileName().toString());
                zos.putNextEntry(zipEntry);
                Files.copy(fileToBackup, zos);
                zos.closeEntry();
            } else {
                throw new IOException("Database file not found at " + dbFilePath);
            }
            
            // Generate basic metadata.json inside zip
            ZipEntry metaEntry = new ZipEntry("metadata.json");
            zos.putNextEntry(metaEntry);
            String metadata = "{ \"timestamp\": \"" + timestamp + "\", \"version\": \"2.0.0\" }";
            zos.write(metadata.getBytes());
            zos.closeEntry();
            
            return backupFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void scheduleAutomaticBackups() {
        // Stub: Setup a timer or executor for daily/weekly backups
    }
    
    public String exportAsJSON() {
        // Stub: Convert DB entries to JSON string
        return "[]"; 
    }
    
    public String exportAsCSV() {
        // Stub: Convert DB entries to CSV format
        return "ID,Title,Type,Priority,Created,Completed\n";
    }
}
