package com.javala.gittravel;

import org.eclipse.jgit.api.errors.GitAPIException;

final class CommandRunner {
    private final Command command;
    private final CommandLineOptions parameters;

    CommandRunner(
        Command command,
        CommandLineOptions parameters
    ) {
        this.command = command;
        this.parameters = parameters;
    }

    int run() throws GitAPIException {
        return command.exec(parameters);
    }
}
