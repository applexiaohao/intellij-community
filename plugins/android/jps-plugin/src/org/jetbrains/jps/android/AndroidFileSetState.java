package org.jetbrains.jps.android;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.incremental.storage.ValidityState;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * @author Eugene.Kudelevsky
 */
public class AndroidFileSetState implements ValidityState {
  private final Map<String, Long> myTimestamps;

  public AndroidFileSetState(@NotNull Collection<String> roots) {
    myTimestamps = new HashMap<String, Long>();

    for (String resourceDir : roots) {
      FileUtil.processFilesRecursively(new File(resourceDir), new Processor<File>() {
        @Override
        public boolean process(File file) {
          myTimestamps.put(FileUtil.toSystemIndependentName(file.getPath()), file.lastModified());
          return true;
        }
      });
    }
  }


  public AndroidFileSetState(DataInput in) throws IOException {
    final int resourcesCount = in.readInt();
    myTimestamps = new HashMap<String, Long>(resourcesCount);

    for (int i = 0; i < resourcesCount; i++) {
      final String filePath = in.readUTF();
      final long timestamp = in.readLong();

      myTimestamps.put(filePath, timestamp);
    }
  }

  @Override
  public boolean equalsTo(ValidityState otherState) {
    return otherState instanceof AndroidFileSetState &&
           ((AndroidFileSetState)otherState).myTimestamps.equals(myTimestamps);
  }

  @Override
  public void save(DataOutput out) throws IOException {
    out.writeInt(myTimestamps.size());

    for (Map.Entry<String, Long> entry : myTimestamps.entrySet()) {
      out.writeUTF(entry.getKey());
      out.writeLong(entry.getValue());
    }
  }
}
