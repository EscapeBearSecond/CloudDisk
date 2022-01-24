package com.dyg.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.hadoop.fs.Path;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HdfsFile {
    private Path prePath;
    private String name;
    private boolean isDir;
    private long len;
    private Path path;
}
