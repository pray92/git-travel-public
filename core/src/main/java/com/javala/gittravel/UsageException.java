package com.javala.gittravel;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

/** 커맨드 라인 인자 값 파싱 과정에서 발생하는 예외, 메시지 출력 후 예외 처리합니다. */
final class UsageException extends Exception {

    private static final Joiner NEWLINE_JOINER = Joiner.on(System.lineSeparator());

    private static final String[] USAGE = {
        "",
        "Usage : git-travel command [flag] [option]",
        "",
        "Command : ",
        "1. init [<branch>] : The git-travel tool initializes the git log entries of <branch>. If <branch> is omitted, tool will initialize master branch or main if master is not present.",
        "2. travel [-b] [<ncommits>] : move forward <ncommits> from HEAD. If -b flag is set, it will move backwards.",
        "3. here : show the commit hash of HEAD.",
        "4. move start|last|end|<commithash> : moves to start commit, last commit, or <commithash> of the initialized branch.",

    };

    UsageException() {
        super(buildMessage(null));
    }

    UsageException(String message) {
        super(buildMessage(Preconditions.checkNotNull(message)));
    }

    private static String buildMessage(String message) {
        StringBuilder builder = new StringBuilder();
        if(null != message){
            appendLine(builder, message);
        }
        appendLines(builder, USAGE);

        return builder.toString();
    }

    private static void appendLines(StringBuilder builder, String[] lines) {
        NEWLINE_JOINER.appendTo(builder, lines).append(System.lineSeparator());
    }

    private static void appendLine(StringBuilder builder, String line){
        builder.append(line).append(System.lineSeparator());
    }
}
