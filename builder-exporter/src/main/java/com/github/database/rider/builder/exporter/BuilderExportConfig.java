package com.github.database.rider.builder.exporter;

import com.github.database.rider.builder.exporter.model.BuilderType;

import java.io.File;

public class BuilderExportConfig {

    private BuilderType type;

    private File outputDir;

    public BuilderExportConfig(BuilderType type, File outputDir) {
        this.type = type;
        this.outputDir = outputDir;
    }

    public BuilderType getType() {
        return type;
    }

    public File getOutputDir() {
        return outputDir;
    }
}
