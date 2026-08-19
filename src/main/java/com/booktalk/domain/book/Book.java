package com.booktalk.domain.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String isbn;

	@Column(nullable = false)
	private String title;

	private String author;

	private String publisher;

	private String coverImageUrl;

	private Integer pageCount;

	// 책등 시각화 관련 (도서 최초 등록 시 1회 생성 후 캐싱)
	private String spineImageUrl;

	private String primaryColor; // #RRGGBB

	private String accentColor; // #RRGGBB

	@Builder
	public Book(String isbn, String title, String author, String publisher,
				String coverImageUrl, Integer pageCount) {
		this.isbn = isbn;
		this.title = title;
		this.author = author;
		this.publisher = publisher;
		this.coverImageUrl = coverImageUrl;
		this.pageCount = pageCount;
	}

	public void updateSpineAssets(String spineImageUrl, String primaryColor, String accentColor) {
		this.spineImageUrl = spineImageUrl;
		this.primaryColor = primaryColor;
		this.accentColor = accentColor;
	}
}
