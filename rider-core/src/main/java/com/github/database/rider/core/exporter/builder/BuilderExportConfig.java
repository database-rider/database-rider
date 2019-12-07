package com.github.database.rider.core.exporter.builder;


import com.github.database.rider.core.api.exporter.BuilderType;

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
