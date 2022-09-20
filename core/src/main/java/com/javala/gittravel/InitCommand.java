package com.javala.gittravel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * <em>init</em> 커맨드를 수행하는 클래스입니다.
 * 
 * <p>
 * 처음 git-travel을 수행하기 위해선 현재 HEAD가 가리키고 있는 {@code 브랜치}
 * 정보가 필요합니다. 이에 대한 정보를 경로에 아래와 같이 구성합니다.
 * 
 * <p>
 * git-travel-data <p>
 * |--projectA <p>
 * | |--.current-branch <p>
 * | |--branchA <p>
 * | | |--head <p>
 * | | |--commits <p>
 * | |--branchB <p>
 * | |--head <p>
 * | |--commits <p>
 * 
 * <p>
 * 상대 디렉토리) <p>
 * Unix: ~/.data/git-travel-data <p>
 * Windows: %LOCALAPPDATA%\git-travel-data
 * 
 * <p>
 * 여기서 인자 값 {@code 브랜치}가 비어 있으면 자동적으로 {@code main} 또는 {@code master}
 * 브랜치 정보를 초기화합니다.
 * 
 * <p>
 * Usage) {@code init [<branch>]}
 */
final class InitCommand extends Command {

    /** init 커맨드를 수행합니다. */
    @Override
    int exec(CommandLineOptions parameters) throws GitAPIException {
        try (Git git = GitTravels.git()){
            final String projectName = GitTravels.getProjectName();
            final TravelLogs travelLogs = TravelLogs.create(projectName);

            String branch = getBranch(git, parameters.branchName());
            if (null == branch) {
                System.out.println("No branch found named : " + branch);
                return 1;
            }

            Iterable<RevCommit> revCommits = git.log().add(git.getRepository().resolve(branch)).call();
            List<String> commits = new ArrayList<String>();
            for (RevCommit revCommit : revCommits) {
                commits.add(revCommit.getName());
            }

            travelLogs.writeCommits(branch.toString(), commits);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return 1;
        } catch (TravelLogsException e) {
            System.err.println(e.getMessage());
            return 1;
        }

        return 0;
    }

    /**
     * 인자로 받은 브랜치 명을 기반으로 브랜치 이름을 반환합니다.
     * 
     * <p>
     * 인자 값이 비었을 경우 {@code main} 또는 {@code master}를 반환합니다.
     * 
     * @param git <em>.git</em>에 대한 정보
     * @param arg 브랜치 이름
     * @return 브랜치 이름, 인자 값이 비었을 경우 {@code main} 또는 {@code master}
     * @throws GitAPIException
     */
    private String getBranch(Git git, Optional<String> arg) throws GitAPIException {
        String ret = null;
        for (Ref branch : git.branchList().call()) {
            String branchName = branch.getName().substring(branch.getName().lastIndexOf('/') + 1);
            switch (branchName) {
                case "master":
                case "main":
                    if (arg.isEmpty()) {
                        ret = branchName;
                    }
                    break;
                default:
                    if (arg.equals(Optional.of(branchName))) {
                        ret = branchName;
                    }
                    break;
            }
        }
        return ret;
    }
}
