package com.javala.gittravel;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * <em>here</em> 커맨드를 수행하는 클래스입니다.
 * 
 * <p>
 * <em>init</em> 커맨드를 통해 초기화된 현재 브랜치 정보를 기반으로
 * 현재 브랜치의 커밋 정보와 HEAD가 가리키고 있는 커밋을 강조 표시합니다. 
 * 
 * <p>
 * Usage) {@code here}
 * 
 * <p>
 * WARNING) <em>init</em>을 통해 가리키는 브랜치 정보를 초기화한 후에 정상 수행가능합니다.
 */
final class HereCommand extends Command {

    /** here 커맨드를 수행합니다. */
    @Override
    int exec(CommandLineOptions parameters) throws GitAPIException {
        try {
            GitTravels.here();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        return 0;
    }
}
