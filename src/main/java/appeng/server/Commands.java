package appeng.server;

import appeng.server.subcommands.ChunkLogger;
import appeng.server.subcommands.Supporters;

public enum Commands
{
	Chunklogger(4, new ChunkLogger()), supporters(0, new Supporters());

	public final int level;
	public final ISubCommand command;

	@Override
	public String toString()
	{
		return name();
	}

	private Commands(int level, ISubCommand w) {
		this.level = level;
		command = w;
	}

}
