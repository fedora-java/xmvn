/*-
 * Copyright (c) 2023-2024 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.xmvn.mojo;

import static org.objectweb.asm.Opcodes.ASM9;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;

/**
 * @author Mikolaj Izdebski
 */
class ModuleGleaner {
    private String gleanAutomaticFromManifest(Manifest mf) {
        if (mf != null) {
            String autoName = mf.getMainAttributes().getValue("Automatic-Module-Name");
            if (autoName != null) {
                return autoName;
            }
        }
        return null;
    }

    private String gleanAutomaticFromJar(Path jarPath) {
        try (JarInputStream jis = new JarInputStream(Files.newInputStream(jarPath))) {
            return gleanAutomaticFromManifest(jis.getManifest());
        } catch (IOException e) {
            return null;
        }
    }

    private String gleanAutomatic(Path path) {
        if (path.getFileName().toString().endsWith(".jar")) {
            return gleanAutomaticFromJar(path);
        }
        return null;
    }

    private String gleanFromModuleInfoClass(InputStream inputStream) throws IOException {
        String[] moduleName = new String[1];
        ClassVisitor classVisitor =
                new ClassVisitor(ASM9) {
                    @Override
                    public ModuleVisitor visitModule(
                            String modName, int modAccess, String modVersion) {
                        moduleName[0] = modName;
                        return null;
                    }
                };
        new ClassReader(inputStream).accept(classVisitor, 0);
        return moduleName[0];
    }

    private String gleanModuleInfoFromJar(Path jarPath) {
        try (JarInputStream jis = new JarInputStream(Files.newInputStream(jarPath))) {
            for (JarEntry entry; (entry = jis.getNextJarEntry()) != null; ) {
                if ("module-info.class".equals(entry.getName())) {
                    String moduleName = gleanFromModuleInfoClass(jis);
                    if (moduleName != null) {
                        return moduleName;
                    }
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String gleanModuleInfoFromClasses(Path classesPath) {
        Path moduleInfoPath = classesPath.resolve("module-info.class");
        if (Files.isRegularFile(moduleInfoPath)) {
            try (InputStream is = Files.newInputStream(moduleInfoPath)) {
                return gleanFromModuleInfoClass(is);
            } catch (IOException e) {
            }
        }
        return null;
    }

    private String gleanModuleInfo(Path path) {
        if (Files.isDirectory(path)) {
            return gleanModuleInfoFromClasses(path);
        }
        if (path.getFileName().toString().endsWith(".jar")) {
            return gleanModuleInfoFromJar(path);
        }
        return null;
    }

    public JavadocModule glean(
            Path artifactPath,
            List<Path> sourcePaths,
            List<Path> dependencies,
            boolean ignoreJPMS) {
        String moduleName = null;
        boolean isAutomatic = false;
        if (!ignoreJPMS) {
            moduleName = gleanModuleInfo(artifactPath);
            if (moduleName == null) {
                moduleName = gleanAutomatic(artifactPath);
                isAutomatic = moduleName != null;
            }
        }
        return new JavadocModule(moduleName, isAutomatic, artifactPath, sourcePaths, dependencies);
    }
}
