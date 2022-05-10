# 4장. (계좌 송금) 유스케이스 구현하기

이번 장에서는 앞에서 소개한 육각형 아키텍처 스타일에서 **유스케이스** 를 구현하는 방법을 소개한다. <br>
육각형 아키텍처 스타일에서 도메인 중심의 아키텍처이다. <br>
때문에 **유스케이스 구현은 도메인 엔티티를 만드는 것으로 시작한다.**

## 도메인 엔티티 구현

입금과 출금을 기능을 가진 Account 엔티티를 만들어보자.

```java
/**
 * 계좌 엔티티
 */
public class Account {
	
	private final AccountId id;
    
    // 계좌의 최근 거래 내역들
    private final ActivityWindow activityWindow;

    // ActivityWindow 의 첫번째 Activity 바로 전의 잔고
    private final Money baselineBalance;

    // 계좌의 잔고 계산 
    public Money calculateBalance() {
		return Money.add(
				this.baselineBalance,
				this.activityWindow.calculateBalance(this.id));
	}

    // 출금 하기
	public boolean withdraw(Money money, AccountId targetAccountId) {
		if (!mayWithdraw(money)) {
			return false;
		}

		Activity withdrawal = new Activity(
				this.id,
				this.id,
				targetAccountId,
				LocalDateTime.now(),
				money);
		this.activityWindow.addActivity(withdrawal);
		return true;
	}

    // 입금 하기
	public boolean deposit(Money money, AccountId sourceAccountId) {
		Activity deposit = new Activity(
				this.id,
				sourceAccountId,
				this.id,
				LocalDateTime.now(),
				money);
		this.activityWindow.addActivity(deposit);
		return true;
	}

    // 출금 가능한지 검사
    private boolean mayWithdraw(Money money) {
        return Money.add(
                        this.calculateBalance(),
                        money.negate())
                .isPositiveOrZero();
    }

}
```

* 한 계좌에서 일어나는 입금과 출금은 deposit(), withdraw() 메서드에서 Activity 추가를 통해 구현한다.
* 계좌 잔고를 초과하는 금액은 출금할 수 없도록 하는 **비즈니스 규칙**이 있다.

입출금 할 수 있는 **Account 중심으로 유스케이스를 구현해보자.**

## 유스케이스 구현

유스케이스는 보통 다음과 같은 로직들이 존재한다.

1. 인커밍 어댑터로 부터 입력을 받는다. (입력 유효성 검증은 다른곳에서...)
2. **비즈니스 규칙을 검증한다. (with 도메인 엔티티)**
3. **엔티티 상태를 조작한다.** 
4. 출력을 반환한다.

송금하기 유스케이스 경우 아래와 같은 구조가 될 것이다. 

```java
public class SendMoneyService implements SendMoneyUseCase {

	private final LoadAccountPort loadAccountPort;
	
	private final UpdateAccountStatePort updateAccountStatePort;

	@Override
	public boolean sendMoney(SendMoneyCommand command) {
		// 비즈니스 규칙 검증

		// 엔티티 상태 조작

		// 출력 반환
	}

}
```

### 입력 유효성 검증
입력 유효성 검증은 유스케이스 클래스 책임은 아니지만, 애플리케이션 계층에서 입력 유효성 검증을 하지 않을 수는 없다. <br>
애플리케이션 코어 바깥쪽으로 부터 유효하지 않은 입력값을 받게 되면 엔티티 상태를 해칠 수 있기 때문이다. <br>
유스케이스 클래스가 아니면 어디에서 입력 유효성을 검증하면 좋을까?

유스케이스의 입력 모델 클래스에서 입력 유효성을 검증하는게 좋다.

* 유스케이스의 호출자 마다 입력 유효성 검증 코드를 넣지 않아도 된다.
* 유스케이스 입력 모델은 유스케이스 인터페이스의 일부이기 때문에 유스케이스 구현 코드를 오염시키지 않는다.
* Bean Validation API 의 도움을 받아서 객체 생성 시 유효성 검증을 자동으로 할 수 있다.

```java
@Value
@EqualsAndHashCode(callSuper = false)
public
class SendMoneyCommand extends SelfValidating<SendMoneyCommand> {

    @NotNull
    private final AccountId sourceAccountId;

    @NotNull
    private final AccountId targetAccountId;

    @NotNull
    private final Money money;

    public SendMoneyCommand(
            AccountId sourceAccountId,
            AccountId targetAccountId,
            Money money) {
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.money = money;
        this.validateSelf();
    }
}
```

입력 모델의 유효성 검증 코드로 애플리케이션 (코어) 내부에 유효하지 않은 입력이 들어오지 못하도록 하자.

### 생성자의 힘

### 유스케이스마다 다른 입력 모델

## 비즈니스 규칙 검증하기

## 풍부한 도메인 모델 vs 빈약한 도메인 모델

## 유스케이스마다 다른 출력 모델

## 읽기 전용 유스케이스

## 유지보수 가능한 소프트웨어를 만드는데 어떻게 도움이 될까?

