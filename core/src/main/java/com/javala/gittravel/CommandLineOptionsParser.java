package com.javala.gittravel;

import java.util.Iterator;
/**
 * {@code CommandLineOptionsParser}는 git-travel 수행시 받아온 인자 값을 기반으로 옵션을 설정하는 클래스입니다.
 * Builder 패턴을 활용해 유동적인 매개변수 초기화와 동시에 불변 파라미터 객체를 생성합니다.
 * 
 * <p>
 * git-travel 명령 룰을 기반으로 비정상적인 값이 들어오면 {@link UsageException} 예외를 던져
 * 프로세스를 강제 종료시키고 메뉴얼을 표기합니다.
 * 
 * <p>
 * {@link IllegalArgumentException}이나 {@link NumberFormatException} 예외를 던지는 경우가 있는데,
 * 이는 {@code parse}를 호출하는 메서드에서 {@link UsageException}으로 감싸 예외를 던지기 때문에
 * 결과적으로 하나의 예외만 처리하게 됩니다. 자세한 활용법은 {@link Main}의 {@code processArgs}를 확인하시면 됩니다.
 */
final class CommandLineOptionsParser {
    /**
     * 인자를 기반으로 {@link CommandLineOptions} 인스턴스를 생성하고 값을 초기화 합니다.
     * 
     * @param options   커맨드 라인 인자
     * @return          필드 값이 초기화된 {@link CommandLineOptionsParser} 인스턴스
     */
    static CommandLineOptions parse(Iterable<String> options) {
        CommandLineOptions.Builder optionsBuilder = CommandLineOptions.builder();

        CommandType commandType = CommandType.NONE;
        Iterator<String> it = options.iterator();
        while (it.hasNext()) {
            String option = it.next();
            String optionToLower = option.toLowerCase();
            switch (optionToLower) {
                case "--help":
                case "-help":
                case "-h":
                    optionsBuilder.help(true);
                    break;
                case "init":
                    commandType = CommandType.INIT;
                    optionsBuilder.commandType(commandType);
                    break;
                case "travel":
                    commandType = CommandType.TRAVEL;
                    optionsBuilder.commandType(commandType);
                    break;
                case "here":
                    commandType = CommandType.HERE;
                    optionsBuilder.commandType(commandType);
                    break;
                case "move":
                    commandType = CommandType.MOVE;
                    optionsBuilder.commandType(commandType);
                    break;
                case "--back":
                case "-back":
                case "-b":
                    optionsBuilder.travelBack(true);
                    break;
                default:
                    switch (commandType) {
                        case INIT:
                            optionsBuilder.branchName(option);
                            break;
                        case TRAVEL:
                            optionsBuilder.travelCount(parseInteger(option));
                            break;
                        case HERE:
                            throw new IllegalArgumentException("\'here\' does not require parameter.");
                        case MOVE:
                            optionsBuilder.moveDestination(option);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid command");
                    }
                    break;
            }
        }

        return optionsBuilder.build();
    }

    /**
     * {@link String} 타입 문자열을 정수 타입 값을 반환합니다.
     * 
     * @param value 문자열
     * @return      문자열 정수 파싱 결과, 문자열 값이 없으면 1을 반환
     */
    private static Integer parseInteger(String value) {
        try {
            return (value.isEmpty()) ? 1 : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid integer value : %s", value), e);
        }
    }
}
