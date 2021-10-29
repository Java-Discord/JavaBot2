package net.javadiscord.javabot2.command;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class CommandPrivilegeConfig {
	private String type;
	private boolean enabled = true;
	private String id;

	// TODO: Reimplement permission deserialization.
//	public SlashCommandPermissions toData(long commandId, Server server) {
//		if (this.type.equalsIgnoreCase(SlashCommandPermissionType.USER.name())) {
//			User user = server.getMemberById(this.id).orElseThrow();
//			return new CommandPrivilege(CommandPrivilege.Type.USER, this.enabled, member.getIdLong());
//		} else if (this.type.equalsIgnoreCase(CommandPrivilege.Type.ROLE.name())) {
//			Long roleId = null;
//			try {
//				roleId = (Long) botConfig.get(guild).resolve(this.id);
//			} catch (UnknownPropertyException e) {
//				log.error("Unknown property while resolving role id.", e);
//			}
//			if (roleId == null) throw new IllegalArgumentException("Missing role id.");
//			Role role = guild.getRoleById(roleId);
//			if (role == null) throw new IllegalArgumentException("Role could not be found for id " + roleId);
//			return new CommandPrivilege(CommandPrivilege.Type.ROLE, this.enabled, role.getIdLong());
//		}
//		throw new IllegalArgumentException("Invalid type.");
//	}
}
