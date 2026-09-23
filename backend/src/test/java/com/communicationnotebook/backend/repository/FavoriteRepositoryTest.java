package com.communicationnotebook.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.communicationnotebook.backend.entity.Favorite;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

// 開発用PostgreSQLに接続して実行する。各テストはロールバックされるため、登録したデータはDBに残らない。
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FavoriteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FavoriteRepository favoriteRepository;

    private User user;
    private User otherUser;
    private User noFavoriteUser;
    private Note firstNote;
    private Note secondNote;
    private Note otherUserFavoriteNote;

    @BeforeEach
    void setUp() {
        user = persistUser("TEST-FAVORITE-REPO-1", "テスト太郎");
        otherUser = persistUser("TEST-FAVORITE-REPO-2", "テスト花子");
        noFavoriteUser = persistUser("TEST-FAVORITE-REPO-3", "テスト次郎");
        firstNote = persistNote("お気に入り対象の投稿1です");
        secondNote = persistNote("お気に入り対象の投稿2です");
        otherUserFavoriteNote = persistNote("別のユーザーのお気に入り投稿です");

        persistFavorite(user, firstNote);
        persistFavorite(user, secondNote);
        persistFavorite(otherUser, otherUserFavoriteNote);
        entityManager.flush();
        entityManager.clear();
    }

    private User persistUser(String employeeId, String name) {
        User newUser = new User();
        newUser.setEmployeeId(employeeId);
        newUser.setName(name);
        newUser.setPassword("dummy");
        newUser.setAdmin(false);
        newUser.setDeleted(false);
        return entityManager.persist(newUser);
    }

    private Note persistNote(String content) {
        Note note = new Note();
        note.setUser(user);
        note.setCategory("業務連絡");
        note.setContent(content);
        note.setDeleted(false);
        return entityManager.persist(note);
    }

    private void persistFavorite(User favoriteUser, Note note) {
        Favorite favorite = new Favorite();
        favorite.setUser(favoriteUser);
        favorite.setNote(note);
        entityManager.persist(favorite);
    }

    @Test
    void findNoteIdsByUserId_returnsOnlyNoteIdsFavoritedByUser() {
        assertThat(favoriteRepository.findNoteIdsByUserId(user.getId()))
                .containsExactlyInAnyOrder(firstNote.getId(), secondNote.getId());
    }

    @Test
    void findNoteIdsByUserId_returnsEmpty_whenUserHasNoFavorites() {
        assertThat(favoriteRepository.findNoteIdsByUserId(noFavoriteUser.getId())).isEmpty();
    }
}
