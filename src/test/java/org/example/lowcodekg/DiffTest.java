package org.example.lowcodekg;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SpringBootTest
public class DiffTest {

    @Test
    public void testJGitUsage() {
        // 构建 Repository 对象
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = null;
        try {
            repository = builder.setGitDir(new File("/Users/chang/Documents/projects/data_projects/NBlog/.git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 定位到目标 commit
        ObjectId commitId = null;
        try {
            commitId = repository.resolve("169e68fe36e119cc7b7bf4b7c3dd4f3e00ae6cb6");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RevWalk revWalk = new RevWalk(repository);
        RevCommit currentCommit = null;
        try {
            currentCommit = revWalk.parseCommit(commitId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 获取前一个 commit
        RevCommit previousCommit = getPreviousCommit(currentCommit, repository);

        // 使用 CanonicalTreeParser 解析树对象
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            try {
                oldTreeIter.reset(reader, previousCommit.getTree().getId());
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, currentCommit.getTree().getId());
                Git git = new Git(repository);
                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTreeIter)
                        .setNewTree(newTreeIter)
                        .call();
                for (DiffEntry diff : diffs) {
                    System.out.println("文件路径：" + diff.getOldPath());
                    System.out.println("修改类型：" + diff.getChangeType());
                }
            } catch (IOException | GitAPIException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private RevCommit getPreviousCommit(RevCommit currentCommit, Repository repository) {
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit parentCommit = revWalk.parseCommit(currentCommit.getParent(0).getId());
            return parentCommit;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
