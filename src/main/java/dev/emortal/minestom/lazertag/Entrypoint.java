package dev.emortal.minestom.lazertag;

import dev.emortal.minestom.core.MinestomServer;
import dev.emortal.minestom.core.module.chat.ChatModule;
import dev.emortal.minestom.core.module.core.CoreModule;
import dev.emortal.minestom.core.module.kubernetes.KubernetesModule;
import dev.emortal.minestom.core.module.permissions.PermissionModule;
import dev.emortal.minestom.gamesdk.GameSdkModule;

public final class Entrypoint {

    public static void main(final String[] args) {
        new MinestomServer.Builder()
                .module(KubernetesModule.class, KubernetesModule::new)
                .module(GameSdkModule.class, GameSdkModule::new)
                .module(CoreModule.class, CoreModule::new)
                .module(PermissionModule.class, PermissionModule::new)
                .module(ChatModule.class, ChatModule::new)
                .module(LazerTagModule.class, LazerTagModule::new)
                .build();
    }
}
