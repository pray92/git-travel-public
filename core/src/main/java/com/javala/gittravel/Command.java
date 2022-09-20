package com.javala.gittravel;

import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * git-travel의 기능을 담당하는 추상 클래스입니다.
 */
abstract class Command {
    /**
     * 커맨드 기능 실행 추상 메서드입니다.
     * 
     * <p>
     * {@link CommandLintOptions} 필드 값을 기반으로 플래그 값을 받아와 실행합니다.
     * 
     * @param parameters 필드 값이 초기화된 인스턴스
     * @return 성공 시 0, 실패 시 1
     * @throws GitAPIException
     */
    abstract int exec(CommandLineOptions parameters) throws GitAPIException;
}
