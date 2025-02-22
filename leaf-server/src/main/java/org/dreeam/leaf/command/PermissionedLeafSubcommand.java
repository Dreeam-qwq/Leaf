package org.dreeam.leaf.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public abstract class PermissionedLeafSubcommand implements LeafSubcommand {

    public final Permission permission;

    protected PermissionedLeafSubcommand(Permission permission) {
        this.permission = permission;
    }

    protected PermissionedLeafSubcommand(String permission, PermissionDefault permissionDefault) {
        this(new Permission(permission, permissionDefault));
    }

    @Override
    public boolean testPermission(@NotNull CommandSender sender) {
        return sender.hasPermission(this.permission);
    }

    @Override
    public @Nullable Permission getPermission() {
        return this.permission;
    }
}
