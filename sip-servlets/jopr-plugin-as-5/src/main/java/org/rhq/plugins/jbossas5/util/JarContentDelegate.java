/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.rhq.plugins.jbossas5.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.content.PackageDetailsKey;
import org.rhq.core.domain.content.transfer.ResourcePackageDetails;

/**
 * Discovers Jar files as artifacts including loading their manifest version into the artifact config.
 *
 * @author Greg Hinkle
 */
public class JarContentDelegate extends FileContentDelegate {
    public static final String MIME_TYPE_JAR = "application/java-archive";

    public JarContentDelegate(File directory, String typeName) {
        super(directory, ".jar", typeName);
    }

    /*
     * public InputStream getContent(ArtifactDetails artifactDetails) {   File contentFile = new File(this.directory,
     * artifactDetails.getArtifactKey());   try   {      return new BufferedInputStream(new
     * FileInputStream(contentFile));   }   catch (FileNotFoundException e)   {      throw new
     * RuntimeException("Artifact content not found for artifact " + artifactDetails, e);   } }
     *
     */
    /*
     * public void deleteContent(ArtifactDetails artifactDetails) {   File contentFile = new File(this.directory,
     * artifactDetails.getArtifactKey());
     *
     * if (!contentFile.exists())      return;
     *
     * // If the artifact is a directory, its contents need to be deleted first   if (contentFile.isDirectory())   {
     *  TomcatFileUtils.deleteDirectoryContents(contentFile.listFiles());   }
     *
     * boolean deleteResult = contentFile.delete();
     *
     * if (deleteResult==false)   {      throw new RuntimeException("Artifact content not succesfully deleted: " +
     * artifactDetails);   }
     *
     * }
     */

    @Override
    public Set<ResourcePackageDetails> discoverDeployedPackages() {
        Set<ResourcePackageDetails> packages = new HashSet<ResourcePackageDetails>();

        File[] files = this.directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(getFileEnding()) && pathname.isFile();
            }
        });

        for (File file : files) {
            JarFile jf = null;
            try {
                Configuration config = new Configuration();
                jf = new JarFile(file);

                Manifest manifest = jf.getManifest();
                String version = null;

                if (manifest != null) {
                    Attributes attributes = manifest.getMainAttributes();

                    version = attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION);

                    config.put(new PropertySimple("version", version));
                    config.put(new PropertySimple("title", attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE)));
                    config.put(new PropertySimple("url", attributes.getValue(Attributes.Name.IMPLEMENTATION_URL)));
                    config
                        .put(new PropertySimple("vendor", attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR)));

                    config.put(new PropertySimple("classpath", attributes.getValue(Attributes.Name.CLASS_PATH)));
                    config.put(new PropertySimple("sealed", attributes.getValue(Attributes.Name.SEALED)));
                }

                if (version == null) {
                    version = "1.0";
                }

                ResourcePackageDetails details = new ResourcePackageDetails(new PackageDetailsKey(file.getName(),
                    version, getPackageTypeName(), "noarch"));

                packages.add(details);
                details.setFileCreatedDate(file.lastModified()); // Why don't we have a last modified time?
                details.setFileName(file.getName());
                details.setFileSize(file.length());
                details.setClassification(MIME_TYPE_JAR);

                details.setExtraProperties(config);
            } catch (IOException e) {
                // If we can't open it, don't worry about it, we just won't know the version
            } finally {
                try {
                    if (jf != null)
                        jf.close();
                } catch (Exception e) {
                    // Nothing we can do here ...
                }
            }
        }

        return packages;
    }
}
