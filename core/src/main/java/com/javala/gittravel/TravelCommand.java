package com.javala.gittravel;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * <em>travel</em> 커맨드를 수행하는 클래스입니다.
 * 
 * <p>
 * 현재 HEAD를 기준으로 앞 또는 뒤로 Options의 {@code travelCount}만큼 이동합니다.
 * 
 * <p>
 * Usage) {@code travel [-b] [<ncommits>]}
 * 
 * <p>
 * WARNING) <em>init</em>을 통해 가리키는 브랜치 정보를 초기화한 후에 정상 수행가능합니다.
 */
public class TravelCommand extends Command {
    /** travel 커맨드를 수행합니다. */
    @Override 
    int exec(CommandLineOptions parameters) throws GitAPIException {
        try {
            int step = (parameters.travelBack()) 
                            ? parameters.travelCount() 
                            : -1 * parameters.travelCount();

            GitTravels.travel(step);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        return 0;
    }
}
