package com.javala.gittravel;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * move 커맨드를 수행하는 클래스입니다.
 * 
 * <p>
 * 현재 브랜치의 특정 해시 커밋으로 이동하거나 키워드 값을 통해
 * 처음과 끝 지점을 이동할 수 있습니다.
 * 
 * <p>
 * Usage) {@code move start|end|last|<commithash>}
 * 
 * <p>
 * WARNING) <em>init</em>을 통해 가리키는 브랜치 정보를 초기화한 후에 정상 수행가능합니다.
 */
final class MoveCommand extends Command {

    /** move 커맨드를 수행합니다. */
    @Override
    int exec(CommandLineOptions parameters) throws GitAPIException {
        try {
            GitTravels.move(parameters.moveDestination().get());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        return 0;
    }
}
