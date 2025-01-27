package eu.faircode.xlua.random.randomizers;

import androidx.annotation.NonNull;

import java.util.List;


import eu.faircode.xlua.random.IRandomizerOld;
import eu.faircode.xlua.random.elements.ISpinnerElement;
import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;

public class RandomKernelNodeName implements IRandomizerOld {
    public static final String[] NODE_NAMES = new String[] { "localhost", "hostname", "android-", "raspberrypi", "ubuntu", "fedora", "debian", "archlinux", "localhost.localdomain", "ubuntu-server", "kali", "centos", "server", "test-server", "docker-desktop" };

    @Override
    public boolean isSetting(String setting) { return setting.equalsIgnoreCase(getSettingName()); }

    @Override
    public String getSettingName() {  return "android.kernel.node.name"; }

    @Override
    public String getName() {
        return "Kernel Node Name";
    }

    @Override
    public String getID() { return "%kernel_node_name%"; }

    @Override
    public String generateString() {
        String nodeName = NODE_NAMES[RandomGenerator.nextInt(0, NODE_NAMES.length)];
        if ("android-".equals(nodeName))
            return nodeName + RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(8, 18), RandomStringGenerator.LOWER_LETTERS);
        return nodeName;
    }

    @Override
    public int generateInteger() { return 0; }

    @Override
    public List<ISpinnerElement> getOptions() { return null; }

    @NonNull
    @Override
    public String toString() { return getName(); }
}
