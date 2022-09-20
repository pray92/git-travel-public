# Git-Travel Spec

## Commands

```
init [<branch>]
travel [-b] [<ncommits>]
here
move start|last|<commithash>
```

- `init`: The git-travel tool initializes the git log entries of \<branch>. If \<branch> is omitted, tool will initialize `master` branch or `main` if `master` is not present.
- `travel`: move forward \<ncommits> from HEAD. If `-b` flag is set, it will move backwards.
- `here`: show the commit hash of HEAD.
- `move`: moves to start commit, last commit, or \<commithash> of the initialized b


## TravelLogs class

`git-travel` CLI는 프로그램을 실행하면서 파일시스템에 상태 정보를 저장한다. 상태 정보를 저장하는 디렉토리 구조는 다음과 같다.

- 최상위 디렉토리 이름은 `git-travel-data`이다. 이를 *상태 디렉토리*라고 한다.
- Git으로 관리되는 프로젝트에서 `git-travel` CLI를 실행하면 그 프로젝트 이름과 동일한 디렉토리가 `git-travel-data` 안에 만들어진다. 이를 *프로젝트 상태 디렉토리*라고 한다.
- *프로젝트 상태 디렉토리* 안에는 `.current-branch` 파일이 존재한다. 이 파일에는 `git-travel` CLI가 현제 추적하고 있는 브랜치 이름이 저장되어 있다. `.current-branch`로 이름을 지은 이유는 git 브랜치 이름이 '.'으로 시작할 수 없기 때문이다.
- `git-travel init <branch>`를 실행하면 *프로젝트 상태 디렉토리* 안에 `<branch>` 이름을 한 디렉토리가 생성된다. 이를 *브랜치 상태 디렉토리*라고 한다.
- *브랜치 상태 디렉토리* 안에는 `head`와 `commits` 파일이 존재한다. `commits`파일은 `<branch>`의 커밋 히스토리가 적혀 있다. `head`파일은 `git-travel` CLI가 `<branch>`에 대해서 추적하고 있는 현재 위치를 commit hash로 저장하고 있다.

projectA에서 `git-travel init branchA` 와 `git-travel init branchB`를 실행하고, projectB에서 `git-travel init branchA`를 실행했으면 다음과 같은 상태 정보가 저장된다.

```bash
git-travel-data
|--projectA
|  |--.current-branch
|  |--branchA
|  |  |--head
|  |  |--commits
|  |--branchB
|     |--head
|     |--commits
|--projectB
   |--.current-branch
   |--branchA
      |--head
      |--commits
```

관례적으로 OS마다 앱이 생성하는 데이터를 저장하는 위치가 정해져 있는데 *상태 디렉토리*를 이곳에 만든다.

- Unix: `~/.data/git-travel-data`
- Windows: `%LOCALAPPDATA%\git-travel-data`
