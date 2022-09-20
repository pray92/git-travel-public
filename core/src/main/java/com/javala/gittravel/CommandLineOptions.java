package com.javala.gittravel;

import java.util.Optional;

/**
 * {@code CommandLineOptions}는 git-travel에 구현된 {@link Command} 상속 인스턴스를
 * 수행할 때 사용하는 필드 값을 지정하는 옵션 클래스입니다.
 * 
 * <p>
 * {@link CommandLineOptionsParser}에서 커맨드 라인 인자 값을 통해 옵션 값을 지정하며,
 * 빌더 패턴을 활용해 유연한 필드 값 초기화 및 불변 객체를 생성할 수 있게 하였습니다.
 * 
 * <p> 자세한 활용법은 {@link CommandLineOptionsParser#parse}를 참고하시면 됩니다.
 */
final class CommandLineOptions {
    private final boolean help;
    private final CommandType commandType;
    private final Optional<String> branchName;
    private final boolean travelBack;
    private final int travelCount;
    private final Optional<String> moveDestination;

    CommandLineOptions(
        Boolean help,
        CommandType commandType,
        Optional<String> branchName,
        boolean travelBack,
        int travelCount,
        Optional<String> moveDestination) {
        this.help = help;
        this.commandType = commandType;
        this.branchName = branchName;
        this.travelBack = travelBack;
        this.travelCount = travelCount;
        this.moveDestination = moveDestination;
    }

    /** 메뉴얼 표기 여부 */
    boolean help() {
        return help;
    }

    /** 수행할 커맨드 타입 */
    CommandType commandType() {
        return commandType;
    }


    Optional<String> branchName() {
        return branchName;
    }

    /** 
     * travel 반대 이동 여부, false일 경우 정방향 이동
     * 
     * <p>
     * 사용처)
     * 1. travel
     */
    boolean travelBack() {
        return travelBack;
    }

    /** 
     * travel 이동 횟수 
     * 
     * <p>
     * 사용처)
     * 1. travel
     * */
    int travelCount() {
        return travelCount;
    }

    /** 
     * 이동할 해시 커밋 값
     * start, last, [<commithash>] 초기화 가능
     * 
     * <p>
     * 사용처)
     * 1. move
     * */
    Optional<String> moveDestination() {
        return moveDestination;
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private boolean help = false;
        private CommandType commandType = CommandType.NONE;
        private Optional<String> branchName = Optional.empty();
        private boolean travelBack = false;
        private int travelCount = 1;
        private Optional<String> moveDestination = Optional.empty();
        
        Builder help(boolean help){
            this.help = help;
            return this;
        }

        Builder commandType(CommandType commandType) {
            this.commandType = commandType;
            return this;
        }

        Builder branchName(String branchName){
            this.branchName = Optional.of(branchName);
            return this;
        }

        Builder travelBack(boolean travelBack){
            this.travelBack = travelBack;
            return this;
        }

        Builder travelCount(int travelCount){
            this.travelCount = travelCount;
            return this;
        }

        Builder moveDestination(String moveDestination){
            this.moveDestination = Optional.of(moveDestination);
            return this;
        }

        CommandLineOptions build() {
            return new CommandLineOptions(
                help,
                commandType, 
                branchName,
                travelBack, 
                travelCount, 
                moveDestination);
        }
    }
}
