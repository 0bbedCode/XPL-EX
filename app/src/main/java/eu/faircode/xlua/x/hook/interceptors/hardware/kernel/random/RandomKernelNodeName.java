package eu.faircode.xlua.x.hook.interceptors.hardware.kernel.random;

import eu.faircode.xlua.utilities.RandomStringGenerator;
import eu.faircode.xlua.x.data.utils.random.RandomGenerator;
import eu.faircode.xlua.x.xlua.settings.random_old.interfaces.IRandomizer;
import eu.faircode.xlua.x.xlua.settings.random_old.RandomElement;

public class RandomKernelNodeName extends RandomElement {
    public static IRandomizer create() { return new RandomKernelNodeName(); }
    public static final String[] NODE_NAMES = new String[] {
            "localhost",
            "hostname",
            "android-",
            "raspberrypi",
            "ubuntu",
            "fedora",
            "debian",
            "archlinux",
            "localhost.localdomain",
            "ubuntu-server",
            "kali",
            "centos",
            "server",
            "test-server",
            "docker-desktop"
    };

    public RandomKernelNodeName() {
        super("Kernel Node Name");
        bindSetting("android.kernel.node.name");
    }

    @Override
    public String generateString() {
        String nodeName = NODE_NAMES[RandomGenerator.nextInt(0, NODE_NAMES.length)];
        if ("android-".equals(nodeName))
            return nodeName + RandomStringGenerator.generateRandomAlphanumericString(RandomGenerator.nextInt(8, 18), RandomStringGenerator.LOWER_LETTERS);
        return nodeName;
    }
}